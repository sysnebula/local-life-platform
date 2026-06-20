package com.localife.platform.module.takeout.dish.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("tb_dish")
public class Dish {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long shopId;
    private Long categoryId;
    private String name;
    private String image;
    private String description;
    private Integer price; // 分
    private Integer status; // 0=下架 1=上架
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private List<DishFlavor> flavors;
}
