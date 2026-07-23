package com.petfeeding.platform.module.im.service;

import com.petfeeding.platform.module.im.dto.ConversationVO;
import com.petfeeding.platform.module.im.dto.MessagePageVO;
import com.petfeeding.platform.module.im.entity.Message;
import com.petfeeding.platform.module.user.entity.User;

import java.time.LocalDateTime;
import java.util.List;

public interface ImService {
    /** 按订单获取/创建会话（owner+feeder 唯一），越权返回 403 */
    ConversationVO getOrCreateByOrder(Long orderId, User user);

    /** 我的会话列表（updatedAfter 增量轮询）；非成员会话不会出现 */
    List<ConversationVO> listConversations(User user, LocalDateTime updatedAfter);

    /** 拉消息（游标分页，倒序取更早的），越权返回 403 */
    MessagePageVO listMessages(Long conversationId, Long cursor, int size, User user);

    /** 发送消息，senderId/role 强制取当前登录用户；越权返回 403 */
    Message sendMessage(Long conversationId, String type, String content, User user);

    /** 标记当前用户已读，返回清零后的未读数 */
    int markRead(Long conversationId, User user);
}
