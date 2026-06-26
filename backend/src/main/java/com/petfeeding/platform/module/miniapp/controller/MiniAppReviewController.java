package com.petfeeding.platform.module.miniapp.controller;

import com.petfeeding.platform.common.result.R;
import com.petfeeding.platform.module.review.entity.Review;
import com.petfeeding.platform.module.review.service.ReviewService;
import com.petfeeding.platform.security.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 小程序 — 评价
 */
@RestController
@RequestMapping("/api/miniapp/reviews")
@RequiredArgsConstructor
@Tag(name = "小程序-评价", description = "评价管理")
public class MiniAppReviewController {

    private final ReviewService reviewService;
    private final JwtUtil jwtUtil;

    @PostMapping
    @Operation(summary = "创建评价")
    public R<Review> create(@RequestBody Review review, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = getUserIdOrNull(authHeader);
        if (userId == null) {
            userId = 1L; // 演示模式：使用默认用户ID
        }
        return R.ok(reviewService.createReview(review, userId));
    }

    @GetMapping("/feeder/{feederId}")
    @Operation(summary = "喂养员的评价列表")
    public R<List<Review>> byFeeder(@PathVariable Long feederId) {
        return R.ok(reviewService.listByFeeder(feederId));
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
