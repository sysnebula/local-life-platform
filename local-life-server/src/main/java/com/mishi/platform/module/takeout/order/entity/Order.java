package com.mishi.platform.module.takeout.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_takeout_order")
public class Order {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String orderNumber;
    private Long userId;
    private Long shopId;
    private Integer status;  // 0=待接单 1=已接单 2=配送中 3=已完成 4=已取消
    private Integer paid;    // 0=未支付 1=已支付
    private Integer amount;  // 分
    private String remark;
    private String addressInfo; // 配送地址快照(JSON)
    private String cancelReason;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
