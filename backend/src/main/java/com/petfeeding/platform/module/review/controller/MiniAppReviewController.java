package com.petfeeding.platform.module.review.controller;

import com.petfeeding.platform.common.result.R;
import com.petfeeding.platform.module.review.entity.Review;
import com.petfeeding.platform.module.review.service.ReviewService;
import com.petfeeding.platform.module.user.entity.User;
import com.petfeeding.platform.module.user.service.UserService;
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
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @PostMapping
    @Operation(summary = "创建评价")
    public R<Review> create(@RequestBody Review review, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = getCurrentUser(authHeader);
        if (user == null) {
            return R.fail(401, "请先登录");
        }
        if (!"OWNER".equals(user.getRole())) {
            return R.fail(403, "只有宠物主人才能评价订单");
        }
        return R.ok(reviewService.createReview(review, user.getId()));
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

    private User getCurrentUser(String authHeader) {
        Long userId = getUserIdOrNull(authHeader);
        return userId == null ? null : userService.getById(userId);
    }
}
