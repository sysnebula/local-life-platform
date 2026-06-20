package com.localife.platform.module.shop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_shop")
public class Shop {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String name;
    private Long typeId;
    private Long merchantUserId;
    private String images;
    private String area;
    private String address;
    private Double longitude;
    private Double latitude;
    private Integer avgPrice;
    private Integer sold;
    private Double score;
    private String openHours;
    private String phone;
    private String description;
    private Integer deliveryFee;
    private Integer minOrder;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /**
     * 距离(km) — Redis GEO 计算，非数据库字段
     */
    @TableField(exist = false)
    private Double distance;
}
