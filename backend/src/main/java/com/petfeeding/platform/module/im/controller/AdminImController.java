package com.petfeeding.platform.module.im.controller;

import com.petfeeding.platform.common.result.R;
import com.petfeeding.platform.module.im.dto.AdminConversationVO;
import com.petfeeding.platform.module.im.dto.AdminMessagePageVO;
import com.petfeeding.platform.module.im.service.AdminImService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理后台 — 会话管理（只读查看客户与喂养员聊天，仅 ADMIN）
 */
@RestController
@RequestMapping("/api/admin/im")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "管理后台-会话管理", description = "查看客户与喂养员之间的聊天会话（只读，仅 ADMIN）")
public class AdminImController {

    private final AdminImService adminImService;

    @GetMapping("/conversations")
    @Operation(summary = "会话列表")
    public R<List<AdminConversationVO>> listConversations(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "keyword", required = false) String keyword) {
        return R.ok(adminImService.listConversations(page, size, keyword));
    }

    @GetMapping("/conversations/{id}/messages")
    @Operation(summary = "会话消息记录")
    public R<AdminMessagePageVO> listMessages(
            @PathVariable Long id,
            @RequestParam(value = "cursor", required = false) Long cursor,
            @RequestParam(value = "size", defaultValue = "50") int size) {
        return R.ok(adminImService.listMessages(id, cursor, size));
    }
}