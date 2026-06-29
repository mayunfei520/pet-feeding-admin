package com.petfeeding.platform.module.miniapp.controller;

import com.petfeeding.platform.common.result.R;
import com.petfeeding.platform.module.miniapp.service.MiniAppUserService;
import com.petfeeding.platform.module.user.dto.LoginResultDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/miniapp/auth")
@RequiredArgsConstructor
@Tag(name = "小程序-认证", description = "微信登录 / 密码登录 / 注册接口")
public class MiniAppAuthController {

    private final MiniAppUserService miniAppUserService;

    @PostMapping("/login")
    @Operation(summary = "微信登录（Mock）")
    public R<LoginResultDTO> login(@RequestBody Map<String, String> body) {
        String code = body.get("code");
        String role = body.getOrDefault("role", "OWNER");
        LoginResultDTO result = miniAppUserService.loginByWechat(code, role);
        return R.ok(result);
    }

    @PostMapping("/register")
    @Operation(summary = "手机号注册")
    public R<LoginResultDTO> register(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        String password = body.get("password");
        String nickname = body.getOrDefault("nickname", "");
        LoginResultDTO result = miniAppUserService.register(phone, password, nickname);
        return R.ok(result);
    }

    @PostMapping("/password-login")
    @Operation(summary = "手机号密码登录")
    public R<LoginResultDTO> passwordLogin(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        String password = body.get("password");
        LoginResultDTO result = miniAppUserService.loginByPassword(phone, password);
        return R.ok(result);
    }
}
