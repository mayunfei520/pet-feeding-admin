package com.petfeeding.platform.module.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petfeeding.platform.module.payment.entity.Payment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付 Mapper
 */
@Mapper
public interface PaymentMapper extends BaseMapper<Payment> {
}
