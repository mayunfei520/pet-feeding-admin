package com.petfeeding.platform.module.im.controller;

import com.petfeeding.platform.common.result.R;
import com.petfeeding.platform.module.im.dto.AdminConversationVO;
import com.petfeeding.platform.module.im.dto.AdminMessagePageVO;
import com.petfeeding.platform.module.im.service.AdminImService;
import com.petfeeding.platform.module.user.entity.User;
import com.petfeeding.platform.module.user.service.UserService;
import com.petfeeding.platform.security.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理后台 — 会话管理（只读查看客户与喂养员聊天，仅 ADMIN）
 */
@RestController
@RequestMapping("/api/admin/im")
@RequiredArgsConstructor
@Tag(name = "管理后台-会话管理", description = "查看客户与喂养员之间的聊天会话（只读，仅 ADMIN）")
public class AdminImController {

    private final AdminImService adminImService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @GetMapping("/conversations")
    @Operation(summary = "会话列表")
    public R<List<AdminConversationVO>> listConversations(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (requireAdmin(authHeader) == null) {
            return R.fail(403, "仅管理员可访问");
        }
        return R.ok(adminImService.listConversations(page, size, keyword));
    }

    @GetMapping("/conversations/{id}/messages")
    @Operation(summary = "会话消息记录")
    public R<AdminMessagePageVO> listMessages(
            @PathVariable Long id,
            @RequestParam(value = "cursor", required = false) Long cursor,
            @RequestParam(value = "size", defaultValue = "50") int size,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (requireAdmin(authHeader) == null) {
            return R.fail(403, "仅管理员可访问");
        }
        return R.ok(adminImService.listMessages(id, cursor, size));
    }

    /**
     * 校验调用者是否为 ADMIN：是则返回用户对象，否则返回 null
     */
    private User requireAdmin(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        try {
            String token = authHeader.replace("Bearer ", "");
            Long userId = jwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                return null;
            }
            User user = userService.getById(userId);
            if (user == null || !"ADMIN".equals(user.getRole())) {
                return null;
            }
            return user;
        } catch (Exception e) {
            return null;
        }
    }
}
