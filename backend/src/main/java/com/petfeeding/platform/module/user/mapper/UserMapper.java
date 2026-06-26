package com.petfeeding.platform.module.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petfeeding.platform.module.user.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
