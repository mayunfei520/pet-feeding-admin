package com.petfeeding.platform.module.user.controller;

import com.petfeeding.platform.common.result.R;
import com.petfeeding.platform.module.user.service.MiniAppUserService;
import com.petfeeding.platform.module.user.dto.LoginResultDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/miniapp/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "小程序-认证", description = "微信登录 / 密码登录 / 注册接口")
public class MiniAppAuthController {

    private final MiniAppUserService miniAppUserService;
    private final ObjectMapper objectMapper;

    @PostMapping("/login")
    @Operation(summary = "微信登录（Mock）")
    public R<LoginResultDTO> login(HttpServletRequest request) {
        Map<String, String> body = normalizeBody(request);
        String code = body.get("code");
        String role = body.getOrDefault("role", "OWNER");
        log.info("小程序微信登录请求: role={}, codeHash={}, ip={}", role, safeHash(code), getClientIp(request));
        LoginResultDTO result = miniAppUserService.loginByWechat(code, role);
        log.info("小程序微信登录成功: userId={}, username={}, role={}", result.getUserId(), result.getUsername(), result.getRole());
        return R.ok(result);
    }

    @PostMapping("/register")
    @Operation(summary = "手机号注册")
    public R<LoginResultDTO> register(HttpServletRequest request) {
        Map<String, String> body = normalizeBody(request);
        String phone = body.get("phone");
        String password = body.get("password");
        String nickname = body.getOrDefault("nickname", "");
        String code = body.get("code");
        String gender = body.get("gender");
        String realName = body.get("realName");
        log.info("小程序注册请求: phone={}, nickname={}, realName={}, ip={}", maskPhone(phone), nickname, maskName(realName), getClientIp(request));
        LoginResultDTO result = miniAppUserService.register(phone, password, nickname, code, gender, realName);
        log.info("小程序注册成功: userId={}, username={}, phone={}, role={}",
            result.getUserId(), result.getUsername(), maskPhone(phone), result.getRole());
        return R.ok(result);
    }

    @PostMapping("/send-code")
    @Operation(summary = "发送注册验证码（模拟）")
    public R<String> sendCode(HttpServletRequest request) {
        Map<String, String> body = normalizeBody(request);
        String phone = body.get("phone");
        log.info("小程序发送验证码请求: phone={}, ip={}", maskPhone(phone), getClientIp(request));
        String code = miniAppUserService.sendRegisterCode(phone);
        return R.ok(code);
    }

    @PostMapping("/password-login")
    @Operation(summary = "手机号密码登录")
    public R<LoginResultDTO> passwordLogin(HttpServletRequest request) {
        Map<String, String> body = normalizeBody(request);
        String phone = body.get("phone");
        String password = body.get("password");
        log.info("小程序密码登录请求: phone={}, ip={}", maskPhone(phone), getClientIp(request));
        LoginResultDTO result = miniAppUserService.loginByPassword(phone, password);
        log.info("小程序密码登录成功: userId={}, username={}, role={}",
            result.getUserId(), result.getUsername(), result.getRole());
        return R.ok(result);
    }

    @PostMapping("/phone-login")
    @Operation(summary = "微信手机号一键登录/注册")
    public R<LoginResultDTO> phoneLogin(HttpServletRequest request) {
        Map<String, String> body = normalizeBody(request);
        String code = body.get("code");
        if (code == null || code.trim().isEmpty()) {
            log.warn("小程序手机号登录失败: code为空, ip={}", getClientIp(request));
            return R.fail("授权信息缺失，请重新授权");
        }
        log.info("小程序手机号登录请求: ip={}", getClientIp(request));
        LoginResultDTO result = miniAppUserService.loginByPhone(code);
        log.info("小程序手机号登录成功: userId={}, username={}, role={}",
            result.getUserId(), result.getUsername(), result.getRole());
        return R.ok(result);
    }

    private Map<String, String> normalizeBody(HttpServletRequest request) {
        Map<String, String> result = new HashMap<>();

        try {
            String rawBody = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);
            if (rawBody != null && !rawBody.trim().isEmpty()) {
                String trimmed = rawBody.trim();
                if (looksLikeJson(request.getContentType(), trimmed)) {
                    Map<String, Object> jsonBody = objectMapper.readValue(trimmed, new TypeReference<Map<String, Object>>() {});
                    jsonBody.forEach((key, value) -> {
                        if (value != null) {
                            result.putIfAbsent(key, String.valueOf(value));
                        }
                    });
                    return result;
                }

                for (String pair : rawBody.split("&")) {
                    String[] parts = pair.split("=", 2);
                    if (parts.length == 2 && !parts[0].isEmpty()) {
                        result.put(parts[0], java.net.URLDecoder.decode(parts[1], StandardCharsets.UTF_8.name()));
                    }
                }
            }
        } catch (Exception e) {
            log.warn("小程序认证请求体解析失败: contentType={}, ip={}, message={}",
                request.getContentType(), getClientIp(request), e.getMessage());
        }

        putIfAbsent(result, "phone", request.getParameter("phone"));
        putIfAbsent(result, "password", request.getParameter("password"));
        putIfAbsent(result, "nickname", request.getParameter("nickname"));
        putIfAbsent(result, "code", request.getParameter("code"));
        putIfAbsent(result, "role", request.getParameter("role"));
        return result;
    }

    private boolean looksLikeJson(String contentType, String rawBody) {
        if (rawBody == null || rawBody.isEmpty()) {
            return false;
        }
        if (rawBody.startsWith("{") || rawBody.startsWith("[")) {
            return true;
        }
        return contentType != null && contentType.toLowerCase().contains(MediaType.APPLICATION_JSON_VALUE);
    }

    private void putIfAbsent(Map<String, String> target, String key, String value) {
        if (!target.containsKey(key) && value != null && !value.isEmpty()) {
            target.put(key, value);
        }
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone == null ? "" : phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    private String maskName(String name) {
        if (name == null || name.isEmpty()) {
            return "";
        }
        if (name.length() == 1) {
            return name;
        }
        return name.charAt(0) + "**";
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.trim().isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private int safeHash(String value) {
        return value == null ? 0 : value.hashCode();
    }
}
