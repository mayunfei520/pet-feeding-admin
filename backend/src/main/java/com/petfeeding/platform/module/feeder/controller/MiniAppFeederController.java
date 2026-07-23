package com.petfeeding.platform.module.feeder.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petfeeding.platform.common.result.R;
import com.petfeeding.platform.module.feeder.entity.Feeder;
import com.petfeeding.platform.module.feeder.service.FeederService;
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
    private final UserService userService;
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
        User user = getCurrentUser(authHeader);
        if (user == null) {
            return R.fail(401, "请先登录");
        }
        if ("ADMIN".equals(user.getRole())) {
            return R.fail(403, "管理员不能提交喂养员申请");
        }

        // 判重：按登录用户 userId 查询（与登录态一致，匿名请求已在上方被 401 拦截）
        LambdaQueryWrapper<Feeder> query = new LambdaQueryWrapper<>();
        query.eq(Feeder::getUserId, user.getId());
        List<Feeder> existList = feederService.list(query);
        Feeder existing = existList.isEmpty() ? null : existList.get(0);

        if (existing != null) {
            String st = existing.getStatus();
            if ("PENDING".equals(st)) {
                // 已有待审核申请：避免重复提交，提示审核中
                return R.fail(409, "您已提交申请，正在审核中");
            }
            if ("APPROVED".equals(st)) {
                // 已是认证喂养员：无需重复申请
                return R.fail(409, "您已是认证喂养员，无需重复申请");
            }
            // REJECTED 或历史脏数据（status 为空/异常值）：允许重新申请，覆盖更新为 PENDING，
            // 既不堆积重复 userId 脏数据，又解除「提交被拒却无法再申请」的死锁。
            // 仅用本次提交的非空字段更新，避免把原有已填信息误清空。
            if (body.get("realName") != null) existing.setRealName(body.get("realName"));
            if (body.get("idCard") != null) existing.setIdCard(body.get("idCard"));
            if (body.get("serviceArea") != null) existing.setServiceArea(body.get("serviceArea"));
            if (body.get("experience") != null) existing.setExperience(body.get("experience"));
            if (body.get("description") != null) existing.setDescription(body.get("description"));
            existing.setStatus("PENDING");
            existing.setRejectReason(null);
            feederService.updateById(existing);
            return R.ok(existing);
        }

        Feeder feeder = new Feeder();
        feeder.setUserId(user.getId());
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

    private User getCurrentUser(String authHeader) {
        Long userId = getUserIdOrNull(authHeader);
        return userId == null ? null : userService.getById(userId);
    }
}
