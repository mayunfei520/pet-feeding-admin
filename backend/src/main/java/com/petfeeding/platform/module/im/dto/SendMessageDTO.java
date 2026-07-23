package com.petfeeding.platform.module.im.dto;

import lombok.Data;

@Data
public class SendMessageDTO {
    private Long conversationId;
    private String type;   // TEXT / IMAGE
    private String content;
}
