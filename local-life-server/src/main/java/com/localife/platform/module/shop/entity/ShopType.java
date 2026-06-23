package com.localife.platform.module.shop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 店铺类型
 */
@Data
@TableName("tb_shop_type")
public class ShopType {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String name;
    private Integer sort;
}
