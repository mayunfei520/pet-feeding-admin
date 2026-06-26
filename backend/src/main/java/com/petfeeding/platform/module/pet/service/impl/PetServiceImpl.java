package com.petfeeding.platform.module.pet.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petfeeding.platform.module.pet.entity.Pet;
import com.petfeeding.platform.module.pet.mapper.PetMapper;
import com.petfeeding.platform.module.pet.service.PetService;
import org.springframework.stereotype.Service;

/**
 * 宠物服务实现
 */
@Service
public class PetServiceImpl extends ServiceImpl<PetMapper, Pet> implements PetService {
}
