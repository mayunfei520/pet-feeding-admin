package com.petfeeding.platform.module.dashboard.controller;

import com.petfeeding.platform.common.result.R;
import com.petfeeding.platform.module.feeder.service.FeederService;
import com.petfeeding.platform.module.order.service.OrderService;
import com.petfeeding.platform.module.pet.service.PetService;
import com.petfeeding.platform.module.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 仪表盘控制器
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "仪表盘", description = "后台首页统计数据")
public class DashboardController {

    private final UserService userService;
    private final PetService petService;
    private final FeederService feederService;
    private final OrderService orderService;

    @GetMapping("/stats")
    @Operation(summary = "获取统计数据")
    public R<Map<String, Object>> stats() {
        Map<String, Object> data = new HashMap<>();
        data.put("users", userService.count());
        data.put("pets", petService.count());
        data.put("feeders", feederService.lambdaQuery().eq(
                com.petfeeding.platform.module.feeder.entity.Feeder::getStatus, "APPROVED").count());
        data.put("orders", orderService.count());
        data.put("pendingOrders", orderService.lambdaQuery().eq(
                com.petfeeding.platform.module.order.entity.Order::getStatus, "PENDING").count());
        data.put("revenue", BigDecimal.ZERO); // 后续可实现统计
        return R.ok(data);
    }
}
