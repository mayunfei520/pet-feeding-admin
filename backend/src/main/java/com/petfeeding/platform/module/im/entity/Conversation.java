package com.petfeeding.platform.module.im.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("conversations")
public class Conversation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Long ownerId;
    private Long feederId;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private Integer ownerUnread;
    private Integer feederUnread;
    private LocalDateTime createTime;
}
