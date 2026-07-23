package com.petfeeding.platform.module.im.dto;

import com.petfeeding.platform.module.im.entity.Message;
import lombok.Data;

import java.util.List;

@Data
public class MessagePageVO {
    private List<Message> list;
    private Long nextCursor; // 下一页游标（更老消息的边界 id），null 表示没有更多
}
