package com.petfeeding.platform.module.im.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.petfeeding.platform.module.feeder.entity.Feeder;
import com.petfeeding.platform.module.feeder.service.FeederService;
import com.petfeeding.platform.module.im.dto.AdminConversationVO;
import com.petfeeding.platform.module.im.dto.AdminMessagePageVO;
import com.petfeeding.platform.module.im.dto.AdminMessageVO;
import com.petfeeding.platform.module.im.entity.Conversation;
import com.petfeeding.platform.module.im.entity.Message;
import com.petfeeding.platform.module.im.mapper.ConversationMapper;
import com.petfeeding.platform.module.im.mapper.MessageMapper;
import com.petfeeding.platform.module.im.service.AdminImService;
import com.petfeeding.platform.module.order.entity.Order;
import com.petfeeding.platform.module.order.service.OrderService;
import com.petfeeding.platform.module.user.entity.User;
import com.petfeeding.platform.module.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理后台即时聊天查看服务实现（只读，跳过参与者校验）
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminImServiceImpl implements AdminImService {

    private final ConversationMapper conversationMapper;
    private final MessageMapper messageMapper;
    private final UserService userService;
    private final FeederService feederService;
    private final OrderService orderService;

    @Override
    public List<AdminConversationVO> listConversations(int page, int size, String keyword) {
        QueryWrapper<Conversation> qw = new QueryWrapper<>();
        qw.orderByDesc("last_message_time");
        List<Conversation> list = conversationMapper.selectList(qw);
        List<AdminConversationVO> vos = new ArrayList<>();
        for (Conversation c : list) {
            vos.add(toVO(c));
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            String k = keyword.trim().toLowerCase();
            vos = vos.stream().filter(v ->
                    (v.getOwnerName() != null && v.getOwnerName().toLowerCase().contains(k)) ||
                    (v.getFeederName() != null && v.getFeederName().toLowerCase().contains(k)) ||
                    String.valueOf(v.getOrderId()).contains(k)
            ).collect(Collectors.toList());
        }
        int from = Math.max(0, (page - 1) * size);
        int to = Math.min(vos.size(), from + size);
        if (from > vos.size()) return Collections.emptyList();
        return vos.subList(from, to);
    }

    @Override
    public AdminMessagePageVO listMessages(Long conversationId, Long cursor, int size) {
        Conversation conv = conversationMapper.selectById(conversationId);
        if (conv == null) {
            throw new RuntimeException("会话不存在");
        }
        QueryWrapper<Message> qw = new QueryWrapper<>();
        qw.eq("conversation_id", conversationId);
        if (cursor != null) {
            qw.lt("id", cursor);
        }
        qw.orderByDesc("id").last("LIMIT " + (size + 1));
        List<Message> rows = messageMapper.selectList(qw);
        Long nextCursor = null;
        if (rows.size() > size) {
            rows = rows.subList(0, size);
            nextCursor = rows.get(rows.size() - 1).getId();
        }
        // 反转成时间正序（旧 -> 新）便于阅读
        Collections.reverse(rows);
        List<AdminMessageVO> vos = new ArrayList<>();
        for (Message m : rows) {
            vos.add(toMsgVO(m));
        }
        AdminMessagePageVO vo = new AdminMessagePageVO();
        vo.setList(vos);
        vo.setNextCursor(nextCursor);
        return vo;
    }

    private AdminConversationVO toVO(Conversation c) {
        AdminConversationVO vo = new AdminConversationVO();
        vo.setId(c.getId());
        vo.setOrderId(c.getOrderId());
        Order order = orderService.getById(c.getOrderId());
        vo.setOrderNo(order != null ? order.getOrderNo() : null);
        vo.setLastMessage(c.getLastMessage());
        vo.setLastMessageTime(c.getLastMessageTime());
        vo.setOwnerUnread(nz(c.getOwnerUnread()));
        vo.setFeederUnread(nz(c.getFeederUnread()));
        vo.setCreateTime(c.getCreateTime());

        User ou = userService.getById(c.getOwnerId());
        vo.setOwnerId(c.getOwnerId());
        vo.setOwnerName(ou != null
                ? (ou.getRealName() != null ? ou.getRealName() : ou.getUsername())
                : "客户#" + c.getOwnerId());
        vo.setOwnerAvatar(ou != null ? ou.getAvatar() : null);

        User fu = userService.getById(c.getFeederId());
        Feeder feeder = feederService.getOne(new QueryWrapper<Feeder>().eq("user_id", c.getFeederId()));
        vo.setFeederId(c.getFeederId());
        vo.setFeederName(fu != null
                ? (fu.getRealName() != null ? fu.getRealName() : fu.getUsername())
                : "喂养员#" + c.getFeederId());
        vo.setFeederAvatar(fu != null ? fu.getAvatar() : null);
        vo.setFeederCertified(feeder != null && "APPROVED".equals(feeder.getStatus()));
        return vo;
    }

    private AdminMessageVO toMsgVO(Message m) {
        AdminMessageVO vo = new AdminMessageVO();
        vo.setId(m.getId());
        vo.setConversationId(m.getConversationId());
        vo.setSenderId(m.getSenderId());
        vo.setSenderRole(m.getSenderRole());
        vo.setMsgType(m.getType());
        vo.setContent(m.getContent());
        vo.setIsRead(m.getIsRead());
        vo.setCreateTime(m.getCreateTime());
        User su = userService.getById(m.getSenderId());
        vo.setSenderName(su != null
                ? (su.getRealName() != null ? su.getRealName() : su.getUsername())
                : "用户#" + m.getSenderId());
        return vo;
    }

    private int nz(Integer v) {
        return v == null ? 0 : v;
    }
}
