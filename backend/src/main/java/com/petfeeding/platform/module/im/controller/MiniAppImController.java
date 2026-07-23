package com.petfeeding.platform.module.im.controller;

import com.petfeeding.platform.common.result.R;
import com.petfeeding.platform.module.im.dto.ConversationVO;
import com.petfeeding.platform.module.im.dto.MessagePageVO;
import com.petfeeding.platform.module.im.dto.SendMessageDTO;
import com.petfeeding.platform.module.im.entity.Message;
import com.petfeeding.platform.module.feeder.entity.Feeder;
import com.petfeeding.platform.module.feeder.mapper.FeederMapper;
import com.petfeeding.platform.module.im.service.ImService;
import com.petfeeding.platform.module.user.entity.User;
import com.petfeeding.platform.module.user.service.UserService;
import com.petfeeding.platform.security.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 小程序 — 即时聊天（IM）
 * 基础前缀 /api/miniapp，全部 JWT 鉴权；senderId 强制取当前登录用户，禁止前端传入。
 */
@RestController
@RequestMapping("/api/miniapp")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "小程序-即时聊天", description = "会话/消息/已读（轮询增量）")
public class MiniAppImController {

    private final ImService imService;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final FeederMapper feederMapper;

    @GetMapping("/conversations/by-order/{orderId}")
    @Operation(summary = "按订单获取/创建会话（owner+feeder 唯一）")
    public R<ConversationVO> byOrder(@PathVariable Long orderId,
                                     @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = getCurrentUser(authHeader);
        if (user == null) {
            return R.fail(401, "请先登录");
        }
        return R.ok(imService.getOrCreateByOrder(orderId, user));
    }

    @GetMapping("/conversations")
    @Operation(summary = "我的会话列表（updatedAfter 增量轮询）")
    public R<List<ConversationVO>> list(@RequestParam(required = false) String updatedAfter,
                                        @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = getCurrentUser(authHeader);
        if (user == null) {
            return R.fail(401, "请先登录");
        }
        return R.ok(imService.listConversations(user, parseTime(updatedAfter)));
    }

    @GetMapping("/conversations/by-feeder/{feederId}")
    @Operation(summary = "按喂养员获取会话列表（feeder 视角，feederId=喂养员表主键）")
    public R<List<ConversationVO>> byFeeder(@PathVariable Long feederId,
                                            @RequestParam(required = false) String updatedAfter,
                                            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = getCurrentUser(authHeader);
        if (user == null) {
            return R.fail(401, "请先登录");
        }
        Feeder feeder = feederMapper.selectById(feederId);
        if (feeder == null) {
            return R.fail(404, "喂养员不存在");
        }
        if (!user.getId().equals(feeder.getUserId())) {
            return R.fail(403, "无权访问该喂养员的会话");
        }
        return R.ok(imService.listConversations(user, parseTime(updatedAfter)));
    }

    @GetMapping("/conversations/{id}/messages")
    @Operation(summary = "拉消息（游标分页）")
    public R<MessagePageVO> messages(@PathVariable Long id,
                                     @RequestParam(required = false) Long cursor,
                                     @RequestParam(defaultValue = "20") int size,
                                     @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = getCurrentUser(authHeader);
        if (user == null) {
            return R.fail(401, "请先登录");
        }
        return R.ok(imService.listMessages(id, cursor, size, user));
    }

    @PostMapping("/messages")
    @Operation(summary = "发送消息")
    public R<Message> send(@RequestBody SendMessageDTO body,
                           @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = getCurrentUser(authHeader);
        if (user == null) {
            return R.fail(401, "请先登录");
        }
        if (body.getConversationId() == null) {
            return R.fail(400, "conversationId 不能为空");
        }
        return R.ok(imService.sendMessage(body.getConversationId(), body.getType(), body.getContent(), user));
    }

    @PutMapping("/conversations/{id}/read")
    @Operation(summary = "标记当前用户已读，返回清零后的未读数")
    public R<Map<String, Object>> read(@PathVariable Long id,
                                       @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = getCurrentUser(authHeader);
        if (user == null) {
            return R.fail(401, "请先登录");
        }
        int unread = imService.markRead(id, user);
        Map<String, Object> m = new HashMap<>();
        m.put("unreadCount", unread);
        return R.ok(m);
    }

    /* ===== 工具 ===== */

    private User getCurrentUser(String authHeader) {
        Long userId = getUserIdOrNull(authHeader);
        return userId == null ? null : userService.getById(userId);
    }

    private Long getUserIdOrNull(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        try {
            String token = authHeader.replace("Bearer ", "");
            return jwtUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            return null;
        }
    }

    private LocalDateTime parseTime(String s) {
        if (s == null || s.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(s.trim());
        } catch (DateTimeParseException e) {
            try {
                return LocalDateTime.parse(s.trim().replace(' ', 'T'));
            } catch (DateTimeParseException e2) {
                return null;
            }
        }
    }
}
