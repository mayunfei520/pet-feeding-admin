package com.petfeeding.platform.module.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 登录响应
 */
@Data
@AllArgsConstructor
public class LoginResultDTO {

    private String token;

    private Long userId;

    private String username;

    private String role;

    /** 真实姓名（注册实名，可能为空） */
    private String realName;
}
