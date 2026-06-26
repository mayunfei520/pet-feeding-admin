package com.petfeeding.platform.module.payment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付记录表
 */
@Data
@TableName("payments")
public class Payment {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联订单 ID */
    private Long orderId;

    /** 付款用户 ID */
    private Long userId;

    /** 支付金额 */
    private BigDecimal amount;

    /** 支付方式：WECHAT-微信, ALIPAY-支付宝 */
    private String payMethod;

    /** 支付状态：UNPAID-未支付, PAID-已支付, REFUNDED-已退款 */
    private String payStatus;

    /** 第三方交易号 */
    private String transactionId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
