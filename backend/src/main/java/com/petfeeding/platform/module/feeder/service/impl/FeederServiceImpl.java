package com.petfeeding.platform.module.feeder.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petfeeding.platform.module.feeder.entity.Feeder;
import com.petfeeding.platform.module.feeder.mapper.FeederMapper;
import com.petfeeding.platform.module.feeder.service.FeederService;
import org.springframework.stereotype.Service;

/**
 * 喂养员服务实现
 */
@Service
public class FeederServiceImpl extends ServiceImpl<FeederMapper, Feeder> implements FeederService {
}
