package com.petfeeding.platform.module.sms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 短信发送记录表
 */
@Data
@TableName("sms_logs")
public class SmsLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联订单ID */
    private Long orderId;

    /** 接收短信的喂养员ID */
    private Long feederId;

    /** 接收手机号 */
    private String phone;

    /** 短信内容 */
    private String content;

    /** 发送状态：SUCCESS-成功, FAIL-失败 */
    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
