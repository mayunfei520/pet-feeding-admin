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
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
        enrichPhone(list);
        return R.ok(list);
    }

    @GetMapping("/pending")
    @Operation(summary = "待审核喂养员列表")
    public R<List<Feeder>> pending() {
        List<Feeder> list = feederService.lambdaQuery()
                .eq(Feeder::getStatus, "PENDING")
                .list();
        enrichPhone(list);
        return R.ok(list);
    }

    @GetMapping("/rejected")
    @Operation(summary = "已拒绝喂养员列表")
    public R<List<Feeder>> rejected() {
        List<Feeder> list = feederService.lambdaQuery()
                .eq(Feeder::getStatus, "REJECTED")
                .list();
        enrichPhone(list);
        return R.ok(list);
    }

    /** 把关联用户的手机号填充到 Feeder.phone（feeders 表无 phone 字段） */
    private void enrichPhone(List<Feeder> list) {
        if (list == null || list.isEmpty()) return;
        Set<Long> userIds = list.stream().map(Feeder::getUserId).collect(Collectors.toSet());
        Map<Long, User> userMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        for (Feeder f : list) {
            User u = userMap.get(f.getUserId());
            f.setPhone(u != null ? u.getPhone() : null);
        }
    }

    @PutMapping("/{id}/approve")
    @Operation(summary = "审核通过喂养员")
    public R<?> approve(@PathVariable Long id) {
        Feeder feeder = feederService.getById(id);
        if (feeder != null) {
            feeder.setStatus("APPROVED");
            feeder.setRejectReason(null);
            feederService.updateById(feeder);
            // 同步将用户角色置为 FEEDER，使小程序可切换喂养员模式 (#7)
            User user = userService.getById(feeder.getUserId());
            if (user != null) {
                user.setRole("FEEDER");
                userService.updateById(user);
            }
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
            // 回退用户角色为 OWNER（若此前已认证），避免残留 FEEDER (#7)
            User user = userService.getById(feeder.getUserId());
            if (user != null && "FEEDER".equals(user.getRole())) {
                user.setRole("OWNER");
                userService.updateById(user);
            }
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
