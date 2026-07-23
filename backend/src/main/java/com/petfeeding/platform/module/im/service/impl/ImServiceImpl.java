package com.petfeeding.platform.module.im.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petfeeding.platform.common.exception.BusinessException;
import com.petfeeding.platform.module.feeder.entity.Feeder;
import com.petfeeding.platform.module.feeder.mapper.FeederMapper;
import com.petfeeding.platform.module.im.dto.ConversationVO;
import com.petfeeding.platform.module.im.dto.MessagePageVO;
import com.petfeeding.platform.module.im.dto.PeerInfo;
import com.petfeeding.platform.module.im.entity.Conversation;
import com.petfeeding.platform.module.im.entity.Message;
import com.petfeeding.platform.module.im.mapper.ConversationMapper;
import com.petfeeding.platform.module.im.mapper.MessageMapper;
import com.petfeeding.platform.module.im.service.ImService;
import com.petfeeding.platform.module.order.entity.Order;
import com.petfeeding.platform.module.order.mapper.OrderMapper;
import com.petfeeding.platform.module.user.entity.User;
import com.petfeeding.platform.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImServiceImpl extends ServiceImpl<ConversationMapper, Conversation> implements ImService {

    private final ConversationMapper conversationMapper;
    private final MessageMapper messageMapper;
    private final OrderMapper orderMapper;
    private final FeederMapper feederMapper;
    private final UserMapper userMapper;

    @Override
    public ConversationVO getOrCreateByOrder(Long orderId, User user) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        Long ownerId = order.getOwnerId();
        Long feederId = order.getFeederId();
        if (ownerId == null || feederId == null) {
            throw new BusinessException(400, "该订单暂无可沟通对象");
        }
        if (!user.getId().equals(ownerId) && !user.getId().equals(feederId)) {
            throw new BusinessException(403, "无权访问该订单会话");
        }
        Conversation conv = lambdaQuery()
                .eq(Conversation::getOrderId, orderId)
                .eq(Conversation::getOwnerId, ownerId)
                .eq(Conversation::getFeederId, feederId)
                .one();
        if (conv == null) {
            conv = new Conversation();
            conv.setOrderId(orderId);
            conv.setOwnerId(ownerId);
            conv.setFeederId(feederId);
            conv.setOwnerUnread(0);
            conv.setFeederUnread(0);
            save(conv);
        }
        return toVO(conv, user);
    }

    @Override
    public List<ConversationVO> listConversations(User user, LocalDateTime updatedAfter) {
        List<Conversation> list = lambdaQuery()
                .and(q -> q.eq(Conversation::getOwnerId, user.getId())
                        .or().eq(Conversation::getFeederId, user.getId()))
                .gt(updatedAfter != null, Conversation::getLastMessageTime, updatedAfter)
                .orderByDesc(Conversation::getLastMessageTime)
                .list();
        return list.stream().map(c -> toVO(c, user)).collect(Collectors.toList());
    }

    @Override
    public MessagePageVO listMessages(Long conversationId, Long cursor, int size, User user) {
        Conversation conv = getById(conversationId);
        if (conv == null) {
            throw new BusinessException(404, "会话不存在");
        }
        if (!user.getId().equals(conv.getOwnerId()) && !user.getId().equals(conv.getFeederId())) {
            throw new BusinessException(403, "无权访问该会话");
        }
        int limit = Math.max(1, Math.min(size, 50));
        List<Message> msgs = messageMapper.selectList(new QueryWrapper<Message>()
                .eq("conversation_id", conversationId)
                .lt(cursor != null, "id", cursor)
                .orderByDesc("id")
                .last("LIMIT " + limit));
        Collections.reverse(msgs);
        MessagePageVO vo = new MessagePageVO();
        vo.setList(msgs);
        vo.setNextCursor(msgs.size() < limit ? null : (msgs.isEmpty() ? null : msgs.get(0).getId()));
        return vo;
    }

    @Override
    public Message sendMessage(Long conversationId, String type, String content, User user) {
        Conversation conv = getById(conversationId);
        if (conv == null) {
            throw new BusinessException(404, "会话不存在");
        }
        if (!user.getId().equals(conv.getOwnerId()) && !user.getId().equals(conv.getFeederId())) {
            throw new BusinessException(403, "无权向该会话发送消息");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException(400, "消息内容不能为空");
        }
        Message msg = new Message();
        msg.setConversationId(conversationId);
        msg.setSenderId(user.getId());
        msg.setSenderRole("OWNER".equals(user.getRole()) ? "OWNER" : "FEEDER");
        msg.setType(type == null || type.trim().isEmpty() ? "TEXT" : type.trim().toUpperCase());
        msg.setContent(content.trim());
        msg.setIsRead(false);
        messageMapper.insert(msg);

        String role = user.getRole();
        if ("OWNER".equals(role)) {
            conv.setFeederUnread((conv.getFeederUnread() == null ? 0 : conv.getFeederUnread()) + 1);
        } else {
            conv.setOwnerUnread((conv.getOwnerUnread() == null ? 0 : conv.getOwnerUnread()) + 1);
        }
        String summary = content.trim();
        conv.setLastMessage(summary.length() > 60 ? summary.substring(0, 60) : summary);
        conv.setLastMessageTime(LocalDateTime.now());
        updateById(conv);
        return msg;
    }

    @Override
    public int markRead(Long conversationId, User user) {
        Conversation conv = getById(conversationId);
        if (conv == null) {
            throw new BusinessException(404, "会话不存在");
        }
        if (!user.getId().equals(conv.getOwnerId()) && !user.getId().equals(conv.getFeederId())) {
            throw new BusinessException(403, "无权访问该会话");
        }
        int cleared;
        if (user.getId().equals(conv.getOwnerId())) {
            cleared = conv.getOwnerUnread() == null ? 0 : conv.getOwnerUnread();
            if (cleared > 0) {
                conv.setOwnerUnread(0);
                updateById(conv);
                messageMapper.update(null, new UpdateWrapper<Message>()
                        .eq("conversation_id", conversationId)
                        .eq("sender_role", "FEEDER")
                        .set("is_read", 1));
            }
        } else {
            cleared = conv.getFeederUnread() == null ? 0 : conv.getFeederUnread();
            if (cleared > 0) {
                conv.setFeederUnread(0);
                updateById(conv);
                messageMapper.update(null, new UpdateWrapper<Message>()
                        .eq("conversation_id", conversationId)
                        .eq("sender_role", "OWNER")
                        .set("is_read", 1));
            }
        }
        return cleared;
    }

    private ConversationVO toVO(Conversation conv, User user) {
        ConversationVO vo = new ConversationVO();
        vo.setId(conv.getId());
        vo.setOrderId(conv.getOrderId());
        vo.setLastMessage(conv.getLastMessage());
        vo.setLastMessageTime(conv.getLastMessageTime());
        vo.setCreateTime(conv.getCreateTime());

        Long myId = user.getId();
        Long peerId = myId.equals(conv.getOwnerId()) ? conv.getFeederId() : conv.getOwnerId();
        String peerRole = myId.equals(conv.getOwnerId()) ? "FEEDER" : "OWNER";
        PeerInfo peer = new PeerInfo();
        peer.setId(peerId);
        peer.setRole(peerRole);
        if ("FEEDER".equals(peerRole)) {
            Feeder feeder = feederMapper.selectOne(
                    new QueryWrapper<Feeder>().eq("user_id", peerId).last("LIMIT 1"));
            if (feeder != null) {
                peer.setName(feeder.getRealName());
                peer.setCertified("APPROVED".equals(feeder.getStatus()));
                User u = userMapper.selectById(peerId);
                peer.setAvatar(u != null ? u.getAvatar() : null);
            }
        } else {
            User u = userMapper.selectById(peerId);
            if (u != null) {
                peer.setName(u.getRealName() != null ? u.getRealName() : u.getUsername());
                peer.setAvatar(u.getAvatar());
            }
        }
        vo.setPeer(peer);

        if (myId.equals(conv.getOwnerId())) {
            vo.setUnreadCount(conv.getOwnerUnread() == null ? 0 : conv.getOwnerUnread());
        } else {
            vo.setUnreadCount(conv.getFeederUnread() == null ? 0 : conv.getFeederUnread());
        }
        return vo;
    }
}
