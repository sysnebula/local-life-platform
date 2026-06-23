package com.localife.platform.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户类型枚举
 */
@Getter
@AllArgsConstructor
public enum UserTypeEnum {

    CUSTOMER(0, "顾客"),
    MERCHANT(1, "商家/店长");

    private final Integer code;
    private final String desc;

    public static UserTypeEnum fromCode(Integer code) {
        for (UserTypeEnum value : values()) {
            if (value.code.equals(code)) return value;
        }
        return CUSTOMER;
    }
}
