package com.petfeeding.platform.module.sms.service.impl;

import com.petfeeding.platform.module.feeder.entity.Feeder;
import com.petfeeding.platform.module.order.entity.Order;
import com.petfeeding.platform.module.sms.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 短信服务实现
 * 当前为模拟版本，使用日志输出代替真实短信发送
 * 后续可接入阿里云或腾讯云短信 SDK
 */
@Slf4j
@Service
public class SmsServiceImpl implements SmsService {

    // 后续接入真实短信时将以下方法替换为调用SDK

    @Override
    public boolean sendOrderNotify(Feeder feeder, Order order) {
        String phone = getFeederPhone(feeder);
        String periodText;
        switch (order.getServicePeriod()) {
            case "AM":
                periodText = "上午（8:00-12:00）";
                break;
            case "PM":
                periodText = "下午（13:00-17:00）";
                break;
            case "EVENING":
                periodText = "晚上（18:00-21:00）";
                break;
            default:
                periodText = order.getServicePeriod();
                break;
        }

        String content = String.format(
            "【宠物喂养平台】您好%s，您有一条新的上门喂养订单："
            + "服务日期%s，时段%s，地址%s。请登录平台查看详情并联系客户确认。",
            feeder.getRealName(),
            order.getServiceDate(),
            periodText,
            order.getAddress()
        );

        log.info("========== 短信发送（模拟） ==========");
        log.info("接收手机: {}", phone);
        log.info("接收人: {}", feeder.getRealName());
        log.info("短信内容: {}", content);
        log.info("======================================");

        return true;
    }

    /**
     * 获取喂养员手机号（通过user表关联查询）
     */
    private String getFeederPhone(Feeder feeder) {
        // 实际项目中应通过 userService 根据 feeder.getUserId() 查询手机号
        return "138****8888";
    }
}
