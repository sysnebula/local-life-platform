package com.mishi.platform.module.dianping.voucher.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@TableName("tb_voucher_order")
public class VoucherOrder implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;
    private Long voucherId;
    /**
     * 0=未支付 1=已支付 2=已退款 3=已核销
     */
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime payTime;

    /**
     * 用户手机号（非DB字段，查询时填充）
     */
    @TableField(exist = false)
    private String userPhone;

    /**
     * 券标题（非DB字段）
     */
    @TableField(exist = false)
    private String voucherTitle;

    /**
     * 实付金额(分)（非DB字段）
     */
    @TableField(exist = false)
    private Integer payValue;

    /**
     * 券面值(分)（非DB字段）
     */
    @TableField(exist = false)
    private Integer actualValue;

    /**
     * 店铺ID（非DB字段）
     */
    @TableField(exist = false)
    private Long shopId;
}
