package com.petfeeding.platform.module.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petfeeding.platform.common.exception.BusinessException;
import com.petfeeding.platform.module.order.entity.Order;
import com.petfeeding.platform.module.order.mapper.OrderMapper;
import com.petfeeding.platform.module.pet.entity.Pet;
import com.petfeeding.platform.module.pet.mapper.PetMapper;
import com.petfeeding.platform.module.user.dto.LoginDTO;
import com.petfeeding.platform.module.user.dto.LoginResultDTO;
import com.petfeeding.platform.module.user.dto.RegisterDTO;
import com.petfeeding.platform.module.user.entity.User;
import com.petfeeding.platform.module.user.mapper.UserMapper;
import com.petfeeding.platform.module.user.service.UserService;
import com.petfeeding.platform.security.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户服务实现
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final PetMapper petMapper;
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public User register(RegisterDTO dto) {
        // 检查用户名是否已存在
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, dto.getUsername());
        if (this.count(queryWrapper) > 0) {
            throw new BusinessException("用户名已存在");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setRole(dto.getRole());
        user.setStatus("ACTIVE");

        this.save(user);
        return user;
    }

    @Override
    public LoginResultDTO login(LoginDTO dto) {
        // 查询用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, dto.getUsername());
        User user = this.getOne(queryWrapper);

        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        if ("DISABLED".equals(user.getStatus())) {
            throw new BusinessException("账号已被禁用");
        }

        // 验证密码
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 生成 JWT Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        return new LoginResultDTO(token, user.getId(), user.getUsername(), user.getRole());
    }

    @Override
    public List<User> listByRole(String role) {
        List<User> users;
        if (role == null || role.isEmpty()) {
            users = this.list();
        } else {
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getRole, role);
            users = this.list(queryWrapper);
        }

        users.forEach(user -> {
            LambdaQueryWrapper<Pet> petQuery = new LambdaQueryWrapper<>();
            petQuery.eq(Pet::getUserId, user.getId());
            user.setPetCount(petMapper.selectCount(petQuery));

            LambdaQueryWrapper<Order> orderQuery = new LambdaQueryWrapper<>();
            orderQuery.eq(Order::getOwnerId, user.getId());
            user.setOrderCount(orderMapper.selectCount(orderQuery));
        });

        return users;
    }
}
