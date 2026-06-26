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
    public R<Review> create(@RequestBody Review review, @RequestHeader("Authorization") String authHeader) {
        Long userId = getUserId(authHeader);
        return R.ok(reviewService.createReview(review, userId));
    }

    @GetMapping("/feeder/{feederId}")
    @Operation(summary = "喂养员的评价列表")
    public R<List<Review>> byFeeder(@PathVariable Long feederId) {
        return R.ok(reviewService.listByFeeder(feederId));
    }

    private Long getUserId(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return jwtUtil.getUserIdFromToken(token);
    }
}
