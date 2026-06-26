package com.petfeeding.platform.module.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.petfeeding.platform.module.user.dto.LoginDTO;
import com.petfeeding.platform.module.user.dto.LoginResultDTO;
import com.petfeeding.platform.module.user.dto.RegisterDTO;
import com.petfeeding.platform.module.user.entity.User;

import java.util.List;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     */
    User register(RegisterDTO dto);

    /**
     * 用户登录
     */
    LoginResultDTO login(LoginDTO dto);

    /**
     * 按角色过滤用户列表
     */
    List<User> listByRole(String role);
}
