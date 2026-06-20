package com.localife.platform.module.takeout.setmeal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("tb_setmeal")
public class Setmeal {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long shopId;
    private Long categoryId;
    private String name;
    private String image;
    private String description;
    private Integer price;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private List<SetmealDish> dishes;
}
