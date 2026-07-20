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
     * 喂养员接单（旧流程，保留向后兼容）
     */
    void acceptOrder(Long orderId, Long feederId);

    /**
     * 喂养员报价：PENDING -> QUOTED，写入 price
     */
    void quoteOrder(Long orderId, Long feederId, java.math.BigDecimal price);

    /**
     * 客户同意报价：QUOTED -> ACCEPTED
     */
    void confirmOrder(Long orderId, Long ownerId);

    /**
     * 客户拒绝报价：QUOTED -> PENDING（保留 price 历史）
     */
    void rejectOrder(Long orderId, Long ownerId);

    /**
     * 完成订单
     */
    void completeOrder(Long orderId, Long userId);

    /**
     * 开始服务
     */
    void startOrder(Long orderId, Long feederId);

    /**
     * 取消订单
     */
    void cancelOrder(Long orderId, Long userId);

    /**
     * 分配喂养员并发送短信通知
     */
    void assignOrder(Long orderId, Long feederId);
}
