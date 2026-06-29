package com.petfeeding.platform.module.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户表
 */
@Data
@TableName("users")
public class User {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户名 */
    private String username;

    /** 密码（BCrypt 加密） */
    private String password;

    /** 手机号 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 角色：OWNER-宠物主人, FEEDER-喂养员, ADMIN-管理员 */
    private String role;

    /** 头像 URL */
    private String avatar;

    /** 状态：ACTIVE-正常, DISABLED-禁用 */
    private String status;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 宠物数量，仅用于后台列表展示 */
    @TableField(exist = false)
    private Long petCount;

    /** 消费次数，仅用于后台列表展示 */
    @TableField(exist = false)
    private Long orderCount;
}
