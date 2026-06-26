package com.petfeeding.platform.module.miniapp.controller;

import com.petfeeding.platform.common.result.R;
import com.petfeeding.platform.module.miniapp.service.MiniAppUserService;
import com.petfeeding.platform.module.user.dto.LoginResultDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 小程序 — 认证接口
 */
@RestController
@RequestMapping("/api/miniapp/auth")
@RequiredArgsConstructor
@Tag(name = "小程序-认证", description = "微信登录接口")
public class MiniAppAuthController {

    private final MiniAppUserService miniAppUserService;

    @PostMapping("/login")
    @Operation(summary = "微信登录")
    public R<LoginResultDTO> login(@RequestBody Map<String, String> body) {
        String code = body.get("code");
        String role = body.getOrDefault("role", "OWNER");
        LoginResultDTO result = miniAppUserService.loginByWechat(code, role);
        return R.ok(result);
    }
}
