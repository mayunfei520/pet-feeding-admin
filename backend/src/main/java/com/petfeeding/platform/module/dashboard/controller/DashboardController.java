package com.petfeeding.platform.module.dashboard.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petfeeding.platform.common.result.R;
import com.petfeeding.platform.module.feeder.entity.Feeder;
import com.petfeeding.platform.module.feeder.service.FeederService;
import com.petfeeding.platform.module.order.entity.Order;
import com.petfeeding.platform.module.order.service.OrderService;
import com.petfeeding.platform.module.payment.entity.Payment;
import com.petfeeding.platform.module.payment.service.PaymentService;
import com.petfeeding.platform.module.pet.service.PetService;
import com.petfeeding.platform.module.user.entity.User;
import com.petfeeding.platform.module.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    private final PaymentService paymentService;

    @GetMapping("/stats")
    @Operation(summary = "获取统计数据")
    public R<Map<String, Object>> stats() {
        Map<String, Object> data = new HashMap<>();

        // ---- 基础统计 ----
        data.put("owners", userService.lambdaQuery().eq(User::getRole, "OWNER").count());
        data.put("admins", userService.lambdaQuery().eq(User::getRole, "ADMIN").count());
        data.put("pets", petService.count());
        data.put("feeders", feederService.lambdaQuery()
                .eq(Feeder::getStatus, "APPROVED").count());
        data.put("orders", orderService.count());
        data.put("pendingOrders", orderService.lambdaQuery()
                .eq(Order::getStatus, "PENDING").count());

        // ---- 真实营收（已支付订单金额总和） ----
        List<Payment> paidPayments = paymentService.lambdaQuery()
                .eq(Payment::getPayStatus, "PAID").list();
        BigDecimal revenue = paidPayments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        data.put("revenue", revenue);

        // ---- 订单状态分布 ----
        Map<String, Long> orderStatusBreakdown = new LinkedHashMap<>();
        String[] statuses = {"PENDING", "QUOTED", "ACCEPTED", "IN_PROGRESS", "COMPLETED", "CANCELLED"};
        for (String status : statuses) {
            long count = orderService.lambdaQuery().eq(Order::getStatus, status).count();
            orderStatusBreakdown.put(status, count);
        }
        data.put("orderStatusBreakdown", orderStatusBreakdown);

        // ---- 订单支付/完成分布（成功 / 待支付 / 付款 / 取消） ----
        // 付款相关状态来自 Payment 表，完成/取消来自 Order 表，合并为消费者视角的 4 段
        long paidCount = paymentService.lambdaQuery().eq(Payment::getPayStatus, "PAID").count();
        long unpaidCount = paymentService.lambdaQuery().eq(Payment::getPayStatus, "UNPAID").count();
        long completedOrders = orderService.lambdaQuery().eq(Order::getStatus, "COMPLETED").count();
        long cancelledOrders = orderService.lambdaQuery().eq(Order::getStatus, "CANCELLED").count();
        Map<String, Long> payStatusBreakdown = new LinkedHashMap<>();
        payStatusBreakdown.put("成功", completedOrders);
        payStatusBreakdown.put("待支付", unpaidCount);
        payStatusBreakdown.put("付款", paidCount);
        payStatusBreakdown.put("取消", cancelledOrders);
        data.put("payStatusBreakdown", payStatusBreakdown);

        // ---- 近7天每日订单趋势 ----
        List<Map<String, Object>> orderTrend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = LocalDate.now().minusDays(i);
            LocalDateTime startOfDay = day.atStartOfDay();
            LocalDateTime endOfDay = day.plusDays(1).atStartOfDay();
            long count = orderService.lambdaQuery()
                    .ge(Order::getCreatedAt, startOfDay)
                    .lt(Order::getCreatedAt, endOfDay)
                    .count();
            Map<String, Object> point = new HashMap<>();
            point.put("date", day.toString());
            point.put("count", count);
            orderTrend.add(point);
        }
        data.put("orderTrend", orderTrend);

        // ---- 客户（宠物主人）性别分布 ----
        long ownerTotal = userService.lambdaQuery().eq(User::getRole, "OWNER").count();
        long maleOwners = userService.lambdaQuery().eq(User::getRole, "OWNER").eq(User::getGender, "男").count();
        long femaleOwners = userService.lambdaQuery().eq(User::getRole, "OWNER").eq(User::getGender, "女").count();
        Map<String, Long> ownerGenderBreakdown = new LinkedHashMap<>();
        ownerGenderBreakdown.put("男", maleOwners);
        ownerGenderBreakdown.put("女", femaleOwners);
        ownerGenderBreakdown.put("未填", ownerTotal - maleOwners - femaleOwners);
        data.put("ownerGenderBreakdown", ownerGenderBreakdown);

        // ---- 近期订单（最新 5 条） ----
        List<Order> recentOrders = orderService.lambdaQuery()
                .orderByDesc(Order::getCreatedAt)
                .last("LIMIT 5")
                .list();
        data.put("recentOrders", recentOrders.stream().map(o -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", o.getId());
            m.put("orderNo", o.getOrderNo());
            m.put("status", o.getStatus());
            m.put("price", o.getPrice());
            m.put("createdAt", o.getCreatedAt());
            return m;
        }).collect(Collectors.toList()));

        // ---- 近期注册用户（最新 5 条） ----
        List<User> recentUsers = userService.lambdaQuery()
                .orderByDesc(User::getCreatedAt)
                .last("LIMIT 5")
                .list();
        data.put("recentUsers", recentUsers.stream().map(u -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", u.getId());
            m.put("username", u.getUsername());
            m.put("nickname", u.getUsername());
            m.put("role", u.getRole());
            m.put("avatar", u.getAvatar());
            m.put("createdAt", u.getCreatedAt());
            return m;
        }).collect(Collectors.toList()));

        return R.ok(data);
    }
}
