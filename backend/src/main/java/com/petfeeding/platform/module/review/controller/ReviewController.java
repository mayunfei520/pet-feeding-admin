package com.petfeeding.platform.module.review.controller;

import com.petfeeding.platform.common.result.R;
import com.petfeeding.platform.module.review.entity.Review;
import com.petfeeding.platform.module.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评价控制器
 */
@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
@Tag(name = "评价管理", description = "评价管理接口")
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/all")
    @Operation(summary = "所有评价列表")
    public R<List<Review>> listAll() {
        return R.ok(reviewService.list());
    }

    @GetMapping("/feeder/{feederId}")
    @Operation(summary = "查看喂养员的评价列表")
    public R<List<Review>> listByFeeder(@PathVariable Long feederId) {
        List<Review> list = reviewService.lambdaQuery()
                .eq(Review::getFeederId, feederId)
                .orderByDesc(Review::getCreatedAt)
                .list();
        return R.ok(list);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除评价")
    public R<?> remove(@PathVariable Long id) {
        reviewService.removeById(id);
        return R.ok("删除成功");
    }
}
