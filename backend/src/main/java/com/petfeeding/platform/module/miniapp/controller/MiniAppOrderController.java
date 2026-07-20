package com.petfeeding.platform.module.miniapp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petfeeding.platform.common.result.R;
import com.petfeeding.platform.module.feeder.entity.Feeder;
import com.petfeeding.platform.module.feeder.service.FeederService;
import com.petfeeding.platform.module.order.entity.Order;
import com.petfeeding.platform.module.order.service.OrderService;
import com.petfeeding.platform.module.user.entity.User;
import com.petfeeding.platform.module.user.service.UserService;
import com.petfeeding.platform.security.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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
    private final FeederService feederService;
    private final JwtUtil jwtUtil;

    @GetMapping
    @Operation(summary = "我的订单列表")
    public R<List<Order>> list(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = getCurrentUser(authHeader);
        if (user == null) {
            return R.fail(401, "请先登录");
        }
        LambdaQueryWrapper<Order> query = new LambdaQueryWrapper<>();
        if ("FEEDER".equals(user.getRole())) {
            query.eq(Order::getFeederId, user.getId());
        } else if ("OWNER".equals(user.getRole())) {
            query.eq(Order::getOwnerId, user.getId());
        } else {
            return R.fail(403, "管理员请在后台查看订单");
        }
        query.orderByDesc(Order::getCreatedAt);
        return R.ok(orderService.list(query));
    }

    @PostMapping
    @Operation(summary = "创建订单")
    public R<Order> create(@RequestBody Order order, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = getCurrentUser(authHeader);
        if (user == null) {
            return R.fail(401, "请先登录");
        }
        if (!"OWNER".equals(user.getRole())) {
            return R.fail(403, "只有宠物主人才能创建订单");
        }
        return R.ok(orderService.createOrder(order, user.getId()));
    }

    @GetMapping("/pending")
    @Operation(summary = "待接单列表（喂养员视角）")
    public R<List<Order>> pending(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = getCurrentUser(authHeader);
        if (user == null) {
            return R.fail(401, "请先登录");
        }
        if (!"FEEDER".equals(user.getRole())) {
            return R.fail(403, "只有喂养员可以查看待接单列表");
        }
        LambdaQueryWrapper<Order> query = new LambdaQueryWrapper<>();
        query.eq(Order::getStatus, "PENDING");
        query.orderByDesc(Order::getCreatedAt);
        return R.ok(orderService.list(query));
    }

    @PutMapping("/{id}/accept")
    @Operation(summary = "喂养员接单")
    public R<?> accept(@PathVariable Long id, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = getCurrentUser(authHeader);
        if (user == null) {
            return R.fail(401, "请先登录");
        }
        if (!"FEEDER".equals(user.getRole())) {
            return R.fail(403, "只有喂养员可以接单");
        }
        Feeder feeder = feederService.lambdaQuery()
                .eq(Feeder::getUserId, user.getId())
                .eq(Feeder::getStatus, "APPROVED")
                .one();
        if (feeder == null) {
            return R.fail(403, "只有审核通过的喂养员才能接单");
        }
        orderService.acceptOrder(id, user.getId());
        return R.ok("接单成功");
    }

    @PutMapping("/{id}/complete")
    @Operation(summary = "宠物主人确认完成订单")
    public R<?> complete(@PathVariable Long id, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = getCurrentUser(authHeader);
        if (user == null) {
            return R.fail(401, "请先登录");
        }
        if (!"OWNER".equals(user.getRole())) {
            return R.fail(403, "只有宠物主人可以确认完成订单");
        }
        orderService.completeOrder(id, user.getId());
        return R.ok("订单已完成");
    }

    @PutMapping("/{id}/start")
    @Operation(summary = "喂养员开始服务")
    public R<?> start(@PathVariable Long id, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = getCurrentUser(authHeader);
        if (user == null) {
            return R.fail(401, "请先登录");
        }
        if (!"FEEDER".equals(user.getRole())) {
            return R.fail(403, "只有喂养员可以开始服务");
        }
        orderService.startOrder(id, user.getId());
        return R.ok("订单已进入服务中");
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "取消订单")
    public R<?> cancel(@PathVariable Long id, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = getCurrentUser(authHeader);
        if (user == null) {
            return R.fail(401, "请先登录");
        }
        if (!"OWNER".equals(user.getRole())) {
            return R.fail(403, "只有宠物主人可以取消订单");
        }
        orderService.cancelOrder(id, user.getId());
        return R.ok("订单已取消");
    }

    @PutMapping("/{id}/quote")
    @Operation(summary = "喂养员报价")
    public R<?> quote(@PathVariable Long id,
                      @RequestBody Map<String, Object> body,
                      @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = getCurrentUser(authHeader);
        if (user == null) {
            return R.fail(401, "请先登录");
        }
        if (!"FEEDER".equals(user.getRole())) {
            return R.fail(403, "只有喂养员可以报价");
        }
        Feeder feeder = feederService.lambdaQuery()
                .eq(Feeder::getUserId, user.getId())
                .eq(Feeder::getStatus, "APPROVED")
                .one();
        if (feeder == null) {
            return R.fail(403, "只有审核通过的喂养员才能报价");
        }
        Object p = body.get("price");
        if (p == null) {
            return R.fail(400, "报价金额必填");
        }
        BigDecimal price;
        try {
            price = new BigDecimal(p.toString());
        } catch (NumberFormatException e) {
            return R.fail(400, "报价金额格式不正确");
        }
        orderService.quoteOrder(id, user.getId(), price);
        return R.ok("报价成功");
    }

    @PutMapping("/{id}/confirm")
    @Operation(summary = "宠物主人同意报价")
    public R<?> confirm(@PathVariable Long id,
                        @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = getCurrentUser(authHeader);
        if (user == null) {
            return R.fail(401, "请先登录");
        }
        if (!"OWNER".equals(user.getRole())) {
            return R.fail(403, "只有宠物主人可以确认报价");
        }
        orderService.confirmOrder(id, user.getId());
        return R.ok("已同意报价");
    }

    @PutMapping("/{id}/reject")
    @Operation(summary = "宠物主人拒绝报价")
    public R<?> reject(@PathVariable Long id,
                       @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = getCurrentUser(authHeader);
        if (user == null) {
            return R.fail(401, "请先登录");
        }
        if (!"OWNER".equals(user.getRole())) {
            return R.fail(403, "只有宠物主人可以拒绝报价");
        }
        orderService.rejectOrder(id, user.getId());
        return R.ok("已拒绝报价");
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
