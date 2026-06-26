package com.petfeeding.platform.module.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petfeeding.platform.module.order.entity.Order;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单 Mapper
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}
