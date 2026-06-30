package com.mishi.platform.module.dianping.voucher.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VoucherDTO {

    private Long shopId;
    private String title;
    private String subTitle;
    private String rules;
    private Integer payValue;
    private Integer actualValue;

    // 库存（普通券和秒杀券通用，null=不限量）
    private Integer stock;

    // 秒杀券专用
    private LocalDateTime beginTime;
    private LocalDateTime endTime;
}
