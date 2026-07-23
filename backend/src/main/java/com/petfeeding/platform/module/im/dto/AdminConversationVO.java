package com.petfeeding.platform.module.im.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理后台会话视图对象（含双方参与者信息，只读）
 */
@Data
public class AdminConversationVO {

    private Long id;
    private Long orderId;
    private String orderNo;
    private Long ownerId;
    private String ownerName;
    private String ownerAvatar;
    private Long feederId;
    private String feederName;
    private String feederAvatar;
    private Boolean feederCertified;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private Integer ownerUnread;
    private Integer feederUnread;
    private LocalDateTime createTime;
}
