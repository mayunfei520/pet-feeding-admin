package com.petfeeding.platform.module.im.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("messages")
public class Message {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long conversationId;
    private Long senderId;
    private String senderRole; // OWNER / FEEDER
    private String type;       // TEXT / IMAGE
    private String content;
    @TableField("is_read")
    private Boolean isRead;
    private LocalDateTime createTime;
}
