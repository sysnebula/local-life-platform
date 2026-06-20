package com.localife.platform.module.takeout.category.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_category")
public class Category {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long shopId;
    private Integer type; // 1=菜品分类 2=套餐分类
    private String name;
    private Integer sort;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
