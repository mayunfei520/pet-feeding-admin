package com.petfeeding.platform.module.user.dto;

import lombok.Data;

/**
 * 更新用户资料 DTO（手机号 / 邮箱 / 性别）
 */
@Data
public class UpdateProfileDTO {
    private String phone;
    private String email;
    private String gender;
}
