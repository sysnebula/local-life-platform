package com.localife.platform.module.dianping.voucher.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_voucher_order")
public class VoucherOrder {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;
    private Long voucherId;
    /**
     * 支付方式：0=微信支付
     */
    private Integer payType;
    /**
     * 0=未支付 1=已支付 2=已退款 3=已核销
     */
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime payTime;
    private LocalDateTime useTime;
    private LocalDateTime refundTime;
}
