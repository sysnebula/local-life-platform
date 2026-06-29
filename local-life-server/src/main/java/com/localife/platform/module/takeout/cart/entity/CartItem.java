package com.localife.platform.module.takeout.cart.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 购物车项 — Redis Hash 存储，非数据库实体
 */
@Data
public class CartItem implements Serializable {
    private Long shopId;   // 店铺ID，用于多店铺购物车分组
    private Long dishId;
    private Long setmealId;
    private String name;
    private Integer price;
    private Integer number;
    private String flavor; // 选中的口味
}
