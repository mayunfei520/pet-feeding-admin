package com.petfeeding.platform.module.feeder.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petfeeding.platform.module.feeder.entity.Feeder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 喂养员 Mapper
 */
@Mapper
public interface FeederMapper extends BaseMapper<Feeder> {
}
