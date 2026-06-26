package com.petfeeding.platform.module.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 订单表
 */
@Data
@TableName("orders")
public class Order {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 订单编号 */
    private String orderNo;

    /** 宠物主人 ID */
    private Long ownerId;

    /** 喂养员 ID */
    private Long feederId;

    /** 宠物 ID */
    private Long petId;

    /** 服务日期 */
    private LocalDate serviceDate;

    /** 服务时段：AM-上午, PM-下午, EVENING-晚上 */
    private String servicePeriod;

    /** 订单状态：PENDING-待接单, ACCEPTED-已接单, IN_PROGRESS-进行中, COMPLETED-已完成, CANCELLED-已取消 */
    private String status;

    /** 服务地址 */
    private String address;

    /** 备注 */
    private String notes;

    /** 订单金额 */
    private BigDecimal price;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
