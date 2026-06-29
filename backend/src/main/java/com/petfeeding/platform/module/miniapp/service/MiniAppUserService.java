package com.petfeeding.platform.module.miniapp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petfeeding.platform.common.exception.BusinessException;
import com.petfeeding.platform.module.user.dto.LoginResultDTO;
import com.petfeeding.platform.module.user.entity.User;
import com.petfeeding.platform.module.user.mapper.UserMapper;
import com.petfeeding.platform.security.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MiniAppUserService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

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
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        return new LoginResultDTO(token, user.getId(), user.getUsername(), user.getRole());
    }

    @Transactional
    public LoginResultDTO register(String phone, String password, String nickname) {
        if (phone == null || phone.trim().isEmpty()) {
            throw new BusinessException("手机号不能为空");
        }
        phone = phone.trim();
        if (password == null || password.length() < 6) {
            throw new BusinessException("密码至少6位");
        }
        LambdaQueryWrapper<User> query = new LambdaQueryWrapper<>();
        query.eq(User::getPhone, phone);
        if (userMapper.selectCount(query) > 0) {
            throw new BusinessException("该手机号已注册");
        }
        User user = new User();
        user.setPhone(phone);
        user.setUsername(buildUsername(phone, nickname));
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("OWNER");
        user.setStatus("ACTIVE");
        userMapper.insert(user);
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        return new LoginResultDTO(token, user.getId(), user.getUsername(), user.getRole());
    }

    public LoginResultDTO loginByPassword(String phone, String password) {
        if (phone == null || password == null) {
            throw new BusinessException("手机号和密码不能为空");
        }
        LambdaQueryWrapper<User> query = new LambdaQueryWrapper<>();
        query.eq(User::getPhone, phone);
        User user = userMapper.selectOne(query);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException("密码错误");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        return new LoginResultDTO(token, user.getId(), user.getUsername(), user.getRole());
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
}
