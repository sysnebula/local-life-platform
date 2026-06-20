package com.localife.platform.module.dianping.voucher.entity;

import com.baomidou.mybatisplus.annotation.IdType;
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
     * 0=普通券 1=秒杀券
     */
    private Integer type;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
