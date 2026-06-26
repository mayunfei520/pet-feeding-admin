package com.petfeeding.platform.module.sms.service;

import com.petfeeding.platform.module.feeder.entity.Feeder;
import com.petfeeding.platform.module.order.entity.Order;

/**
 * 短信服务接口
 */
public interface SmsService {

    /**
     * 发送订单通知短信给喂养员
     * @param feeder 喂养员信息
     * @param order  订单信息
     * @return 是否发送成功
     */
    boolean sendOrderNotify(Feeder feeder, Order order);
}
