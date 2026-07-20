package com.petfeeding.platform.module.order.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petfeeding.platform.common.exception.BusinessException;
import com.petfeeding.platform.module.feeder.entity.Feeder;
import com.petfeeding.platform.module.feeder.service.FeederService;
import com.petfeeding.platform.module.order.entity.Order;
import com.petfeeding.platform.module.order.mapper.OrderMapper;
import com.petfeeding.platform.module.order.service.OrderService;
import com.petfeeding.platform.module.pet.entity.Pet;
import com.petfeeding.platform.module.pet.service.PetService;
import com.petfeeding.platform.module.sms.service.SmsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    private final PetService petService;
    private final SmsService smsService;

    @Override
    @Transactional
    public Order createOrder(Order order, Long ownerId) {
        // 预约防护：被预约的宠物必须已通过平台审核，待审核/已驳回不可被预约
        Pet pet = petService.getById(order.getPetId());
        if (pet == null) {
            throw new BusinessException("宠物不存在");
        }
        if (!Pet.STATUS_APPROVED.equals(pet.getStatus())) {
            throw new BusinessException("该宠物尚未通过平台审核，暂不可预约");
        }
        order.setOwnerId(ownerId);
        order.setOrderNo(generateOrderNo());
        // 新流程：价格由喂养员报价时写入，创建时不接受客户端传入的金额
        order.setPrice(null);
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
    public void quoteOrder(Long orderId, Long feederId, BigDecimal price) {
        Order order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!"PENDING".equals(order.getStatus())) {
            throw new BusinessException("只有待报价的订单才能报价");
        }
        if (order.getFeederId() == null || !order.getFeederId().equals(feederId)) {
            throw new BusinessException("只有被指定的喂养员才能为该订单报价");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("报价金额必须大于0");
        }
        order.setPrice(price);
        order.setStatus("QUOTED");
        this.updateById(order);
    }

    @Override
    @Transactional
    public void confirmOrder(Long orderId, Long ownerId) {
        Order order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getOwnerId().equals(ownerId)) {
            throw new BusinessException("只有下单用户才能确认订单");
        }
        if (!"QUOTED".equals(order.getStatus())) {
            throw new BusinessException("只有已报价的订单才能确认");
        }
        order.setStatus("ACCEPTED");
        this.updateById(order);
    }

    @Override
    @Transactional
    public void rejectOrder(Long orderId, Long ownerId) {
        Order order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getOwnerId().equals(ownerId)) {
            throw new BusinessException("只有下单用户才能拒绝报价");
        }
        if (!"QUOTED".equals(order.getStatus())) {
            throw new BusinessException("只有已报价的订单才能拒绝");
        }
        // 退回待报价，保留 price 历史供喂养员重报参考
        order.setStatus("PENDING");
        this.updateById(order);
    }

    @Override
    @Transactional
    public void completeOrder(Long orderId, Long userId) {
        Order order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getOwnerId().equals(userId)) {
            throw new BusinessException("只有下单用户才能确认完成订单");
        }
        if (!"ACCEPTED".equals(order.getStatus())) {
            throw new BusinessException("只有已接单的订单才能确认完成");
        }
        order.setStatus("COMPLETED");
        this.updateById(order);
    }

    @Override
    @Transactional
    public void startOrder(Long orderId, Long feederId) {
        Order order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getFeederId() == null || !order.getFeederId().equals(feederId)) {
            throw new BusinessException("只有当前接单喂养员才能开始服务");
        }
        if (!"ACCEPTED".equals(order.getStatus())) {
            throw new BusinessException("只有已接单的订单才能开始服务");
        }
        order.setStatus("IN_PROGRESS");
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
        if (!"PENDING".equals(order.getStatus()) && !"QUOTED".equals(order.getStatus())) {
            throw new BusinessException("只能取消待报价或已报价的订单");
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
            throw new BusinessException("只能分配待报价的订单");
        }

        Feeder feeder = feederService.getById(feederId);
        if (feeder == null) {
            throw new BusinessException("喂养员不存在");
        }
        if (!"APPROVED".equals(feeder.getStatus())) {
            throw new BusinessException("该喂养员尚未通过审核");
        }

        // 订单统一存储喂养员对应的用户 ID，和小程序接单逻辑保持一致
        // 新流程：后台分配仅指定喂养员，订单保持 PENDING，由喂养员随后报价
        order.setFeederId(feeder.getUserId());
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
