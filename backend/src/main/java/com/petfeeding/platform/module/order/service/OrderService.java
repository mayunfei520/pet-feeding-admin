package com.petfeeding.platform.module.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.petfeeding.platform.module.order.entity.Order;

/**
 * 订单服务接口
 */
public interface OrderService extends IService<Order> {

    /**
     * 创建订单
     */
    Order createOrder(Order order, Long ownerId);

    /**
     * 喂养员接单
     */
    void acceptOrder(Long orderId, Long feederId);

    /**
     * 完成订单
     */
    void completeOrder(Long orderId, Long userId);

    /**
     * 取消订单
     */
    void cancelOrder(Long orderId, Long userId);

    /**
     * 分配喂养员并发送短信通知
     */
    void assignOrder(Long orderId, Long feederId);
}
