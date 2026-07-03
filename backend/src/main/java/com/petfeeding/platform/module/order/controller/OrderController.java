package com.petfeeding.platform.module.order.controller;

import com.petfeeding.platform.common.result.R;
import com.petfeeding.platform.module.order.entity.Order;
import com.petfeeding.platform.module.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单控制器
 */
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
@Tag(name = "订单管理", description = "订单的创建、接单、完成、取消")
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/all")
    @Operation(summary = "所有订单列表")
    public R<List<Order>> listAll() {
        return R.ok(orderService.list());
    }

    @PostMapping
    @Operation(summary = "创建订单")
    public R<Order> create(@RequestBody Order order, Authentication authentication) {
        Long ownerId = (Long) authentication.getPrincipal();
        return R.ok(orderService.createOrder(order, ownerId));
    }

    @GetMapping("/my")
    @Operation(summary = "我的订单列表")
    public R<List<Order>> myOrders(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        List<Order> list = orderService.lambdaQuery()
                .eq(Order::getOwnerId, userId)
                .orderByDesc(Order::getCreatedAt)
                .list();
        return R.ok(list);
    }

    @GetMapping("/pending")
    @Operation(summary = "待接单列表")
    public R<List<Order>> pending() {
        List<Order> list = orderService.lambdaQuery()
                .eq(Order::getStatus, "PENDING")
                .orderByDesc(Order::getCreatedAt)
                .list();
        return R.ok(list);
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "取消订单")
    public R<?> cancel(@PathVariable Long id) {
        Order order = orderService.getById(id);
        if (order != null) {
            order.setStatus("CANCELLED");
            orderService.updateById(order);
        }
        return R.ok("订单已取消");
    }

    @PutMapping("/{id}/assign")
    @Operation(summary = "分配喂养员并发送短信通知")
    public R<?> assign(@PathVariable Long id, @RequestParam Long feederId) {
        orderService.assignOrder(id, feederId);
        return R.ok("分配成功，短信已通知喂养员");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除订单")
    public R<?> remove(@PathVariable Long id) {
        orderService.removeById(id);
        return R.ok("删除成功");
    }
}
