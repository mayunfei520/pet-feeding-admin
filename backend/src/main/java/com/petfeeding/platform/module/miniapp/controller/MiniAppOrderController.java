package com.petfeeding.platform.module.miniapp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petfeeding.platform.common.result.R;
import com.petfeeding.platform.module.order.entity.Order;
import com.petfeeding.platform.module.order.service.OrderService;
import com.petfeeding.platform.module.user.entity.User;
import com.petfeeding.platform.module.user.service.UserService;
import com.petfeeding.platform.security.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 小程序 — 订单
 */
@RestController
@RequestMapping("/api/miniapp/orders")
@RequiredArgsConstructor
@Tag(name = "小程序-订单", description = "订单管理（OWNER / FEEDER 双视角）")
public class MiniAppOrderController {

    private final OrderService orderService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @GetMapping
    @Operation(summary = "我的订单列表")
    public R<List<Order>> list(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = getUserIdOrNull(authHeader);
        if (userId == null) {
            // 演示模式或无token：返回所有订单
            return R.ok(orderService.list());
        }
        User user = userService.getById(userId);
        LambdaQueryWrapper<Order> query = new LambdaQueryWrapper<>();
        if (user != null && "FEEDER".equals(user.getRole())) {
            query.eq(Order::getFeederId, userId);
        } else {
            query.eq(Order::getOwnerId, userId);
        }
        query.orderByDesc(Order::getCreatedAt);
        return R.ok(orderService.list(query));
    }

    @PostMapping
    @Operation(summary = "创建订单")
    public R<Order> create(@RequestBody Order order, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = getUserIdOrNull(authHeader);
        if (userId == null) {
            userId = 1L; // 演示模式：使用默认用户ID
        }
        return R.ok(orderService.createOrder(order, userId));
    }

    @GetMapping("/pending")
    @Operation(summary = "待接单列表（喂养员视角）")
    public R<List<Order>> pending() {
        LambdaQueryWrapper<Order> query = new LambdaQueryWrapper<>();
        query.eq(Order::getStatus, "PENDING");
        query.orderByDesc(Order::getCreatedAt);
        return R.ok(orderService.list(query));
    }

    @PutMapping("/{id}/accept")
    @Operation(summary = "喂养员接单")
    public R<?> accept(@PathVariable Long id, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = getUserIdOrNull(authHeader);
        if (userId == null) {
            userId = 1L;
        }
        orderService.acceptOrder(id, userId);
        return R.ok("接单成功");
    }

    @PutMapping("/{id}/complete")
    @Operation(summary = "喂养员完成订单")
    public R<?> complete(@PathVariable Long id, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = getUserIdOrNull(authHeader);
        if (userId == null) {
            userId = 1L;
        }
        orderService.completeOrder(id, userId);
        return R.ok("订单已完成");
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "取消订单")
    public R<?> cancel(@PathVariable Long id, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = getUserIdOrNull(authHeader);
        if (userId == null) {
            userId = 1L;
        }
        orderService.cancelOrder(id, userId);
        return R.ok("订单已取消");
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
