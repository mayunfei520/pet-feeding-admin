package com.petfeeding.platform.module.im.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConversationVO {
    private Long id;
    private Long orderId;
    private PeerInfo peer;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private Integer unreadCount; // 当前用户未读
    private LocalDateTime createTime;
}
