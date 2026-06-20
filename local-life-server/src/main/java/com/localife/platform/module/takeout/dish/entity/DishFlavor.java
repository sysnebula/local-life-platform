package com.localife.platform.module.takeout.dish.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("tb_dish_flavor")
public class DishFlavor {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long dishId;
    private String name;  // 口味名，如"辣度"
    private String value; // 口味值，如"微辣,中辣,特辣"
}
