package com.petfeeding.platform.module.feeder.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.petfeeding.platform.common.handler.IdCardTypeHandler;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 喂养员表
 */
@Data
@TableName(value = "feeders", autoResultMap = true)
public class Feeder {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联用户 ID */
    private Long userId;

    /** 真实姓名 */
    private String realName;

    /** 身份证号（库内 SM4 加密存储，TypeHandler 透明解密） */
    @TableField(typeHandler = IdCardTypeHandler.class)
    private String idCard;

    /** 服务区域（如：朝阳区） */
    private String serviceArea;

    /** 经验描述 */
    private String experience;

    /** 自我介绍 */
    private String description;

    /** 资质证书 URL（多个用逗号分隔） */
    private String certification;

    /** 审核状态：PENDING-待审核, APPROVED-已通过, REJECTED-已拒绝 */
    private String status;

    /** 拒绝原因（status=REJECTED 时记录） */
    private String rejectReason;

    /** 评分（满分5.0） */
    private BigDecimal rating;

    /** 手机号（来自关联用户表，非 feeders 表字段） */
    @TableField(exist = false)
    private String phone;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
