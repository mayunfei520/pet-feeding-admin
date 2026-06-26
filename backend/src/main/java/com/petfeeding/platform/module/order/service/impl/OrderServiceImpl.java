package com.petfeeding.platform.module.order.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petfeeding.platform.common.exception.BusinessException;
import com.petfeeding.platform.module.feeder.entity.Feeder;
import com.petfeeding.platform.module.feeder.service.FeederService;
import com.petfeeding.platform.module.order.entity.Order;
import com.petfeeding.platform.module.order.mapper.OrderMapper;
import com.petfeeding.platform.module.order.service.OrderService;
import com.petfeeding.platform.module.sms.service.SmsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 订单服务实现
 */
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private final FeederService feederService;
    private final SmsService smsService;

    @Override
    @Transactional
    public Order createOrder(Order order, Long ownerId) {
        order.setOwnerId(ownerId);
        order.setOrderNo(generateOrderNo());
        order.setStatus("PENDING");
        this.save(order);
        return order;
    }

    @Override
    @Transactional
    public void acceptOrder(Long orderId, Long feederId) {
        Order order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!"PENDING".equals(order.getStatus())) {
            throw new BusinessException("该订单已被接单或已取消");
        }
        order.setFeederId(feederId);
        order.setStatus("ACCEPTED");
        this.updateById(order);
    }

    @Override
    @Transactional
    public void completeOrder(Long orderId, Long userId) {
        Order order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getFeederId().equals(userId)) {
            throw new BusinessException("只有接单的喂养员才能完成订单");
        }
        if (!"ACCEPTED".equals(order.getStatus())) {
            throw new BusinessException("只有已接单的订单才能标记完成");
        }
        order.setStatus("COMPLETED");
        this.updateById(order);
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId, Long userId) {
        Order order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getOwnerId().equals(userId)) {
            throw new BusinessException("只有下单用户才能取消订单");
        }
        if (!"PENDING".equals(order.getStatus())) {
            throw new BusinessException("只能取消待接单的订单");
        }
        order.setStatus("CANCELLED");
        this.updateById(order);
    }

    @Override
    @Transactional
    public void assignOrder(Long orderId, Long feederId) {
        Order order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!"PENDING".equals(order.getStatus())) {
            throw new BusinessException("只能分配待接单的订单");
        }

        Feeder feeder = feederService.getById(feederId);
        if (feeder == null) {
            throw new BusinessException("喂养员不存在");
        }
        if (!"APPROVED".equals(feeder.getStatus())) {
            throw new BusinessException("该喂养员尚未通过审核");
        }

        // 分配喂养员
        order.setFeederId(feederId);
        order.setStatus("ACCEPTED");
        this.updateById(order);

        // 发送短信通知喂养员
        smsService.sendOrderNotify(feeder, order);
    }

    /**
     * 生成订单编号
     */
    private String generateOrderNo() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int randomPart = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "PF" + datePart + randomPart;
    }
}
