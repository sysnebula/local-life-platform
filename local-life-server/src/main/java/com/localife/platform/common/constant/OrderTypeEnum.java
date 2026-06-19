package com.localife.platform.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 订单类型枚举
 */
@Getter
@AllArgsConstructor
public enum OrderTypeEnum {

    VOUCHER(0, "团购券"),
    TAKEOUT(1, "外卖配送");

    private final Integer code;
    private final String desc;
}
