package com.petfeeding.platform.module.miniapp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petfeeding.platform.common.exception.BusinessException;
import com.petfeeding.platform.common.util.PasswordPolicyUtil;
import com.petfeeding.platform.module.user.dto.LoginResultDTO;
import com.petfeeding.platform.module.user.entity.User;
import com.petfeeding.platform.module.user.mapper.UserMapper;
import com.petfeeding.platform.security.util.JwtUtil;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class MiniAppUserService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    @Value("${wx.miniapp.app-id:}")
    private String wxAppId;

    @Value("${wx.miniapp.app-secret:}")
    private String wxAppSecret;

    private static final String WX_ACCESS_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s";
    private static final String WX_PHONE_URL = "https://api.weixin.qq.com/wxa/business/getuserphonenumber?access_token=%s";

    private volatile String cachedAccessToken;
    private volatile long accessTokenExpiresAt;

    // ===== 短信验证码（模拟模式，后续可替换为真实短信渠道）=====
    private static final boolean SMS_MOCK = true;
    private static final long CODE_TTL_MS = 5 * 60 * 1000L;
    private final Map<String, CodeEntry> codeStore = new ConcurrentHashMap<>();

    @Transactional
    public LoginResultDTO loginByWechat(String code, String role) {
        if (role == null || role.isEmpty()) {
            role = "OWNER";
        }
        String mockOpenId = "wx_" + (code != null ? code.hashCode() : System.currentTimeMillis());
        LambdaQueryWrapper<User> query = new LambdaQueryWrapper<>();
        query.eq(User::getPhone, mockOpenId);
        User user = userMapper.selectOne(query);
        if (user == null) {
            user = new User();
            user.setUsername("wx_" + (code != null ? Integer.toHexString(code.hashCode()) : System.currentTimeMillis() % 100000));
            user.setPassword("$2a$10$N/A");
            user.setPhone(mockOpenId);
            user.setRole(role);
            user.setStatus("ACTIVE");
            userMapper.insert(user);
            log.info("创建微信登录新用户: userId={}, username={}, role={}", user.getId(), user.getUsername(), user.getRole());
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        return new LoginResultDTO(token, user.getId(), user.getUsername(), user.getRole(), user.getRealName());
    }

    @Transactional
    public LoginResultDTO register(String phone, String password, String nickname, String code, String gender, String realName) {
        if (phone == null || phone.trim().isEmpty()) {
            log.warn("小程序注册失败: 手机号为空");
            throw new BusinessException("手机号不能为空");
        }
        phone = phone.trim();
        if (!verifyCode(phone, code)) {
            log.warn("小程序注册失败: 手机号={}, 原因=验证码错误", maskPhone(phone));
            throw new BusinessException("验证码错误或已过期");
        }
        // 密码复杂度校验（后端兜底，前端校验可被直接调用绕过）
        try {
            PasswordPolicyUtil.validate(password);
        } catch (BusinessException e) {
            log.warn("小程序注册失败: 手机号={}, 原因=密码复杂度不达标", maskPhone(phone));
            throw e;
        }
        // 真实姓名校验：2-4 个汉字（与前端正则一致，后端兜底）
        if (realName == null || !realName.trim().matches("^[一-龥]{2,4}$")) {
            log.warn("小程序注册失败: 手机号={}, 原因=真实姓名格式不合法 realName={}", maskPhone(phone), realName);
            throw new BusinessException("请输入真实姓名（2-4个汉字）");
        }
        realName = realName.trim();
        LambdaQueryWrapper<User> query = new LambdaQueryWrapper<>();
        query.eq(User::getPhone, phone);
        if (userMapper.selectCount(query) > 0) {
            log.warn("小程序注册失败: 手机号={}, 原因=手机号已注册", maskPhone(phone));
            throw new BusinessException("该手机号已注册");
        }
        User user = new User();
        user.setPhone(phone);
        user.setUsername(buildUsername(phone, nickname));
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("OWNER");
        user.setStatus("ACTIVE");
        if (gender != null && !gender.trim().isEmpty()) {
            user.setGender(gender.trim());
        }
        user.setRealName(realName);
        userMapper.insert(user);
        log.info("创建小程序注册用户: userId={}, username={}, phone={}, realName={}, role={}",
            user.getId(), user.getUsername(), maskPhone(phone), maskName(realName), user.getRole());
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        return new LoginResultDTO(token, user.getId(), user.getUsername(), user.getRole(), user.getRealName());
    }

    /**
     * 发送注册验证码（模拟模式：生成6位码存入内存，并直接返回给调用方用于测试）
     * 后续接入真实短信时，将日志发送替换为调用腾讯云/阿里云短信 SDK。
     */
    public String sendRegisterCode(String phone) {
        if (phone == null || !phone.matches("^1\\d{10}$")) {
            throw new BusinessException("手机号格式不正确");
        }
        String code = String.format("%06d", ThreadLocalRandom.current().nextInt(1000000));
        codeStore.put(phone, new CodeEntry(code, System.currentTimeMillis() + CODE_TTL_MS));
        if (SMS_MOCK) {
            log.info("========== 模拟短信发送 ==========");
            log.info("接收手机: {}", maskPhone(phone));
            log.info("验证码: {}", code);
            log.info("====================================");
            return code;
        }
        // TODO: 接入真实短信渠道（腾讯云/阿里云 SMS SDK）
        return null;
    }

    /**
     * 校验验证码（一次性，校验成功后删除）
     */
    private boolean verifyCode(String phone, String inputCode) {
        if (inputCode == null || inputCode.trim().isEmpty()) {
            return false;
        }
        CodeEntry entry = codeStore.get(phone);
        if (entry == null) {
            return false;
        }
        if (System.currentTimeMillis() > entry.expireAt) {
            codeStore.remove(phone);
            return false;
        }
        boolean ok = entry.code.equals(inputCode.trim());
        if (ok) {
            codeStore.remove(phone);
        }
        return ok;
    }

    private static class CodeEntry {
        final String code;
        final long expireAt;
        CodeEntry(String code, long expireAt) {
            this.code = code;
            this.expireAt = expireAt;
        }
    }

    public LoginResultDTO loginByPassword(String phone, String password) {
        if (phone == null || password == null) {
            log.warn("小程序密码登录失败: 手机号或密码为空");
            throw new BusinessException("手机号和密码不能为空");
        }
        LambdaQueryWrapper<User> query = new LambdaQueryWrapper<>();
        query.eq(User::getPhone, phone);
        User user = userMapper.selectOne(query);
        if (user == null) {
            log.warn("小程序密码登录失败: 手机号={}, 原因=用户不存在", maskPhone(phone));
            throw new BusinessException("用户不存在");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("小程序密码登录失败: 手机号={}, 原因=密码错误", maskPhone(phone));
            throw new BusinessException("密码错误");
        }
        log.info("小程序密码登录校验通过: userId={}, username={}, role={}",
            user.getId(), user.getUsername(), user.getRole());
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        return new LoginResultDTO(token, user.getId(), user.getUsername(), user.getRole(), user.getRealName());
    }

    @Transactional
    public LoginResultDTO loginByPhone(String code) {
        // 1. 调用微信接口解密手机号
        String phone;
        try {
            phone = getPhoneNumberFromWx(code);
        } catch (Exception e) {
            log.error("微信手机号解密失败: code={}, error={}", code != null ? code.hashCode() : 0, e.getMessage(), e);
            throw new BusinessException("微信授权失败，请重新授权");
        }
        if (phone == null || phone.trim().isEmpty()) {
            log.warn("微信手机号解密返回空手机号: code={}", code != null ? code.hashCode() : 0);
            throw new BusinessException("未获取到手机号，请重新授权");
        }
        phone = phone.trim();
        log.info("微信手机号解密成功: phone={}", maskPhone(phone));

        // 2. 按手机号查找或创建用户
        LambdaQueryWrapper<User> query = new LambdaQueryWrapper<>();
        query.eq(User::getPhone, phone);
        User user = userMapper.selectOne(query);
        if (user == null) {
            user = new User();
            user.setPhone(phone);
            user.setUsername("wx_" + phone.substring(phone.length() - 4));
            user.setPassword(passwordEncoder.encode("wx_" + System.currentTimeMillis()));
            user.setRole("OWNER");
            user.setStatus("ACTIVE");
            userMapper.insert(user);
            log.info("微信手机号登录-创建新用户: userId={}, phone={}, role={}",
                user.getId(), maskPhone(phone), user.getRole());
        } else {
            log.info("微信手机号登录-已有用户: userId={}, phone={}, role={}",
                user.getId(), maskPhone(phone), user.getRole());
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        return new LoginResultDTO(token, user.getId(), user.getUsername(), user.getRole(), user.getRealName());
    }

    /**
     * 调用微信 getuserphonenumber 接口获取手机号
     */
    private String getPhoneNumberFromWx(String code) {
        String accessToken = getWxAccessToken();
        String url = String.format(WX_PHONE_URL, accessToken);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String requestBody = "{\"code\":\"" + code + "\"}";
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            log.error("微信getuserphonenumber响应异常: status={}", response.getStatusCode());
            throw new BusinessException("微信服务异常");
        }

        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            // 检查微信业务错误码
            int errcode = root.path("errcode").asInt();
            if (errcode != 0) {
                String errmsg = root.path("errmsg").asText();
                log.error("微信getuserphonenumber业务失败: errcode={}, errmsg={}", errcode, errmsg);
                throw new BusinessException("获取手机号失败: " + errmsg);
            }
            return root.path("phone_info").path("purePhoneNumber").asText();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("微信getuserphonenumber响应解析失败: body={}", response.getBody(), e);
            throw new BusinessException("微信服务响应异常");
        }
    }

    /**
     * 获取微信 access_token（带简单的内存缓存）
     */
    private synchronized String getWxAccessToken() {
        long now = System.currentTimeMillis();
        if (cachedAccessToken != null && now < accessTokenExpiresAt) {
            return cachedAccessToken;
        }
        String url = String.format(WX_ACCESS_TOKEN_URL, wxAppId, wxAppSecret);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            log.error("获取微信access_token失败: status={}", response.getStatusCode());
            throw new BusinessException("微信服务连接失败");
        }
        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            String token = root.path("access_token").asText();
            int expiresIn = root.path("expires_in").asInt(7200);
            if (token == null || token.isEmpty()) {
                int errcode = root.path("errcode").asInt();
                String errmsg = root.path("errmsg").asText();
                log.error("获取微信access_token失败: errcode={}, errmsg={}", errcode, errmsg);
                throw new BusinessException("微信鉴权失败，请检查AppID/AppSecret配置");
            }
            this.cachedAccessToken = token;
            this.accessTokenExpiresAt = now + (expiresIn - 300) * 1000L; // 提前5分钟刷新
            log.info("微信access_token刷新成功, 有效期{}秒", expiresIn);
            return token;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("微信access_token响应解析失败: body={}", response.getBody(), e);
            throw new BusinessException("微信服务响应异常");
        }
    }

    private String buildUsername(String phone, String nickname) {
        String base = nickname == null || nickname.trim().isEmpty() ? phone : nickname.trim();
        LambdaQueryWrapper<User> query = new LambdaQueryWrapper<>();
        query.eq(User::getUsername, base);
        if (userMapper.selectCount(query) == 0) {
            return base;
        }
        return base + "_" + phone.substring(Math.max(0, phone.length() - 4));
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
}
