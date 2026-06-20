package com.localife.platform.module.dianping.voucher.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 秒杀券（1:1 关联 tb_voucher）
 */
@Data
@TableName("tb_seckill_voucher")
public class SeckillVoucher {

    @TableId
    private Long voucherId;
    private Integer stock;
    private LocalDateTime beginTime;
    private LocalDateTime endTime;
}
