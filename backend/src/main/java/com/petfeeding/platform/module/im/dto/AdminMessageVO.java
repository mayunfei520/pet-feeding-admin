package com.petfeeding.platform.module.im.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理后台消息视图对象（含发送者姓名，只读）
 */
@Data
public class AdminMessageVO {

    private Long id;
    private Long conversationId;
    private Long senderId;
    private String senderName;
    private String senderRole;
    private String msgType;
    private String content;
    private Boolean isRead;
    private LocalDateTime createTime;
}
