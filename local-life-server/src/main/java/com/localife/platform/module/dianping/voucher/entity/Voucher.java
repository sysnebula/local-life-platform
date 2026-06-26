package com.localife.platform.module.dianping.voucher.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_voucher")
public class Voucher {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long shopId;
    private String title;
    private String subTitle;
    private String rules;
    /**
     * 支付金额(分)
     */
    private Integer payValue;
    /**
     * 面值(分)
     */
    private Integer actualValue;
    /**
     * 库存(null=不限量, 0=售罄)
     */
    private Integer stock;
    /**
     * 0=普通券 1=秒杀券
     */
    private Integer type;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // === 秒杀券关联信息（非DB字段，查询时填充） ===
    @TableField(exist = false)
    private Integer seckillStock;
    @TableField(exist = false)
    private LocalDateTime seckillBeginTime;
    @TableField(exist = false)
    private LocalDateTime seckillEndTime;
}
