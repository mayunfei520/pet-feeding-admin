package com.petfeeding.platform.module.payment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petfeeding.platform.module.payment.entity.Payment;
import com.petfeeding.platform.module.payment.mapper.PaymentMapper;
import com.petfeeding.platform.module.payment.service.PaymentService;
import org.springframework.stereotype.Service;

/**
 * 支付服务实现
 */
@Service
public class PaymentServiceImpl extends ServiceImpl<PaymentMapper, Payment> implements PaymentService {
}
