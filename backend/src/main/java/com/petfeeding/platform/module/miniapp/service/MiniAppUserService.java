package com.petfeeding.platform.module.miniapp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petfeeding.platform.common.exception.BusinessException;
import com.petfeeding.platform.module.user.dto.LoginResultDTO;
import com.petfeeding.platform.module.user.entity.User;
import com.petfeeding.platform.module.user.mapper.UserMapper;
import com.petfeeding.platform.security.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 小程序用户服务 — Mock 微信登录
 */
@Service
@RequiredArgsConstructor
public class MiniAppUserService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    @Transactional
    public LoginResultDTO loginByWechat(String code, String role) {
        if (role == null || role.isEmpty()) {
            role = "OWNER";
        }

        // Mock: 用 code 生成一个假 openId，查 users 表
        String mockOpenId = "wx_" + (code != null ? code.hashCode() : System.currentTimeMillis());

        // 尝试通过 phone 字段查（我们临时用 phone 存 openId 做演示）
        LambdaQueryWrapper<User> query = new LambdaQueryWrapper<>();
        query.eq(User::getPhone, mockOpenId);
        User user = userMapper.selectOne(query);

        if (user == null) {
            // 新用户 — 自动注册
            user = new User();
            user.setUsername("wx_" + (code != null ? Integer.toHexString(code.hashCode()) : System.currentTimeMillis() % 100000));
            user.setPassword("$2a$10$N/A"); // 微信用户不需要密码
            user.setPhone(mockOpenId); // 临时用 phone 存 openId
            user.setRole(role);
            user.setStatus("ACTIVE");
            userMapper.insert(user);
        }

        // 生成 JWT
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        return new LoginResultDTO(token, user.getId(), user.getUsername(), user.getRole());
    }
}
