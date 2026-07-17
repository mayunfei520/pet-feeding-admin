package com.petfeeding.platform.module.feeder.controller;

import com.petfeeding.platform.common.result.R;
import com.petfeeding.platform.module.feeder.entity.Feeder;
import com.petfeeding.platform.module.feeder.service.FeederService;
import com.petfeeding.platform.module.user.entity.User;
import com.petfeeding.platform.module.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 喂养员控制器
 */
@RestController
@RequestMapping("/api/feeder")
@RequiredArgsConstructor
@Tag(name = "喂养员管理", description = "喂养员注册、审核、信息管理")
public class FeederController {

    private final FeederService feederService;
    private final UserService userService;

    @GetMapping
    @Operation(summary = "喂养员列表（已通过审核）")
    public R<List<Feeder>> list() {
        List<Feeder> list = feederService.lambdaQuery()
                .eq(Feeder::getStatus, "APPROVED")
                .list();
        return R.ok(list);
    }

    @GetMapping("/pending")
    @Operation(summary = "待审核喂养员列表")
    public R<List<Feeder>> pending() {
        List<Feeder> list = feederService.lambdaQuery()
                .eq(Feeder::getStatus, "PENDING")
                .list();
        return R.ok(list);
    }

    @GetMapping("/rejected")
    @Operation(summary = "已拒绝喂养员列表")
    public R<List<Feeder>> rejected() {
        List<Feeder> list = feederService.lambdaQuery()
                .eq(Feeder::getStatus, "REJECTED")
                .list();
        return R.ok(list);
    }

    @PutMapping("/{id}/approve")
    @Operation(summary = "审核通过喂养员")
    public R<?> approve(@PathVariable Long id) {
        Feeder feeder = feederService.getById(id);
        if (feeder != null) {
            feeder.setStatus("APPROVED");
            feeder.setRejectReason(null);
            feederService.updateById(feeder);
        }
        return R.ok("审核通过");
    }

    @PutMapping("/{id}/reject")
    @Operation(summary = "拒绝喂养员申请")
    public R<?> reject(@PathVariable Long id, @RequestParam String reason) {
        Feeder feeder = feederService.getById(id);
        if (feeder != null) {
            feeder.setStatus("REJECTED");
            feeder.setRejectReason(reason);
            feederService.updateById(feeder);
        }
        return R.ok("已拒绝");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除喂养员")
    public R<?> remove(@PathVariable Long id) {
        Feeder feeder = feederService.getById(id);
        if (feeder != null) {
            feederService.removeById(id);
            User user = userService.getById(feeder.getUserId());
            if (user != null && "FEEDER".equals(user.getRole())) {
                user.setRole("OWNER");
                userService.updateById(user);
            }
        }
        return R.ok("删除成功");
    }
}
