package com.mishi.platform.module.takeout.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mishi.platform.module.takeout.order.entity.Order;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}
