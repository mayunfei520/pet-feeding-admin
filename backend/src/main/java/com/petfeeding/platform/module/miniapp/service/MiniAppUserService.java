package com.petfeeding.platform.module.miniapp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petfeeding.platform.common.exception.BusinessException;
import com.petfeeding.platform.module.user.dto.LoginResultDTO;
import com.petfeeding.platform.module.user.entity.User;
import com.petfeeding.platform.module.user.mapper.UserMapper;
import com.petfeeding.platform.security.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
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
            log.info("创建微信登录新用户: userId={}, username={}, role={}", user.getId(), user.getUsername(), user.getRole());
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        return new LoginResultDTO(token, user.getId(), user.getUsername(), user.getRole());
    }

    @Transactional
    public LoginResultDTO register(String phone, String password, String nickname) {
        if (phone == null || phone.trim().isEmpty()) {
            log.warn("小程序注册失败: 手机号为空");
            throw new BusinessException("手机号不能为空");
        }
        phone = phone.trim();
        if (password == null || password.length() < 6) {
            log.warn("小程序注册失败: 手机号={}, 原因=密码长度不足", maskPhone(phone));
            throw new BusinessException("密码至少6位");
        }
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
        userMapper.insert(user);
        log.info("创建小程序注册用户: userId={}, username={}, phone={}, role={}",
            user.getId(), user.getUsername(), maskPhone(phone), user.getRole());
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        return new LoginResultDTO(token, user.getId(), user.getUsername(), user.getRole());
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

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone == null ? "" : phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}
