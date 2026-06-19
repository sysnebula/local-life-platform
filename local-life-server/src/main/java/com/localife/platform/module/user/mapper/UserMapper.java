package com.localife.platform.module.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.localife.platform.module.user.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
