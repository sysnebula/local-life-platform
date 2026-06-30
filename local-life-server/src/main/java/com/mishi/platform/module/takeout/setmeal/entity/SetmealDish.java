package com.mishi.platform.module.takeout.setmeal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("tb_setmeal_dish")
public class SetmealDish {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long setmealId;
    private Long dishId;
    private String name;
    private Integer price;
    private Integer copies;
}
