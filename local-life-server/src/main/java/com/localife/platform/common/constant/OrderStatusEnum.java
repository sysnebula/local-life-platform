package com.localife.platform.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 订单状态枚举（外卖订单）
 */
@Getter
@AllArgsConstructor
public enum OrderStatusEnum {

    PENDING(0, "待接单"),
    ACCEPTED(1, "已接单"),
    DELIVERING(2, "配送中"),
    COMPLETED(3, "已完成"),
    CANCELLED(4, "已取消");

    private final Integer code;
    private final String desc;
}
