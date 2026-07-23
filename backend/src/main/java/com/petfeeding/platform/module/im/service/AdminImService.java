package com.petfeeding.platform.module.im.service;

import com.petfeeding.platform.module.im.dto.AdminConversationVO;
import com.petfeeding.platform.module.im.dto.AdminMessagePageVO;

import java.util.List;

/**
 * 管理后台即时聊天查看服务（只读）
 */
public interface AdminImService {

    /** 会话列表（按末条消息时间倒序，支持关键字过滤与分页） */
    List<AdminConversationVO> listConversations(int page, int size, String keyword);

    /** 会话消息记录（时间正序，游标分页加载更早的消息） */
    AdminMessagePageVO listMessages(Long conversationId, Long cursor, int size);
}
