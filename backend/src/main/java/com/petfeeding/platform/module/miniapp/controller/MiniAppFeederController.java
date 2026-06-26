package com.petfeeding.platform.module.miniapp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petfeeding.platform.common.exception.BusinessException;
import com.petfeeding.platform.common.result.R;
import com.petfeeding.platform.module.feeder.entity.Feeder;
import com.petfeeding.platform.module.feeder.service.FeederService;
import com.petfeeding.platform.module.review.entity.Review;
import com.petfeeding.platform.module.review.service.ReviewService;
import com.petfeeding.platform.security.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 小程序 — 喂养员
 */
@RestController
@RequestMapping("/api/miniapp/feeders")
@RequiredArgsConstructor
@Tag(name = "小程序-喂养员", description = "喂养员列表、申请")
public class MiniAppFeederController {

    private final FeederService feederService;
    private final ReviewService reviewService;
    private final JwtUtil jwtUtil;

    @GetMapping
    @Operation(summary = "喂养员列表（已审核通过）")
    public R<List<Feeder>> list() {
        LambdaQueryWrapper<Feeder> query = new LambdaQueryWrapper<>();
        query.eq(Feeder::getStatus, "APPROVED");
        return R.ok(feederService.list(query));
    }

    @PostMapping
    @Operation(summary = "申请成为喂养员")
    public R<Feeder> apply(@RequestBody Map<String, String> body, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = getUserIdOrNull(authHeader);
        if (userId == null) {
            userId = 1L; // 演示模式：使用默认用户ID
        }
        // 演示模式兼容：user_id 有唯一约束，演示模式下跳过重复检查只用现有记录
        if (userId == 1L) {
            java.util.List<Feeder> existing = feederService.lambdaQuery()
                    .eq(Feeder::getUserId, 1L)
                    .list();
            if (!existing.isEmpty()) {
                return R.ok(existing.get(0)); // 演示模式下直接返回已有申请
            }
        } else {
            LambdaQueryWrapper<Feeder> query = new LambdaQueryWrapper<>();
            query.eq(Feeder::getUserId, userId);
            long count = feederService.count(query);
            if (count > 0) {
                throw new BusinessException("您已提交过申请，请等待审核");
            }
        }

        Feeder feeder = new Feeder();
        feeder.setUserId(userId);
        feeder.setRealName(body.get("realName"));
        feeder.setIdCard(body.get("idCard"));
        feeder.setServiceArea(body.get("serviceArea"));
        feeder.setExperience(body.get("experience"));
        feeder.setDescription(body.get("description"));
        feeder.setStatus("PENDING");
        feederService.save(feeder);
        return R.ok(feeder);
    }

    @GetMapping("/{id}/reviews")
    @Operation(summary = "喂养员评价列表")
    public R<List<Review>> reviews(@PathVariable Long id) {
        return R.ok(reviewService.listByFeeder(id));
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
}
