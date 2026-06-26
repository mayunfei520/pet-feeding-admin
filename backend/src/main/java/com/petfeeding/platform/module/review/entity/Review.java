package com.petfeeding.platform.module.review.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评价表
 */
@Data
@TableName("reviews")
public class Review {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联订单 ID */
    private Long orderId;

    /** 评价人 ID（宠物主人） */
    private Long ownerId;

    /** 被评价喂养员 ID */
    private Long feederId;

    /** 评分 1-5 */
    private Integer rating;

    /** 评价内容 */
    private String content;

    /** 评价图片（多个 URL 用逗号分隔） */
    private String images;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
