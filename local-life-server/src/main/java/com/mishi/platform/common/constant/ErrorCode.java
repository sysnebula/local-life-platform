package com.mishi.platform.common.constant;

/**
 * 统一错误码
 */
public final class ErrorCode {

    // 40xxx — 客户端错误
    public static final int BAD_REQUEST = 40000;
    public static final int NOT_AUTH = 40100;
    public static final int FORBIDDEN = 40300;
    public static final int NOT_FOUND = 40400;
    public static final int PARAM_INVALID = 40001;

    // 50xxx — 服务端错误
    public static final int SERVER_ERROR = 50000;

    // 60xxx — 业务规则限制
    public static final int STOCK_INSUFFICIENT = 60001;
    public static final int ORDER_NOT_EXISTS = 60002;
    public static final int USER_EXISTS = 60003;
    public static final int PASSWORD_ERROR = 60004;
    public static final int SHOP_NOT_YOURS = 60005;

    private ErrorCode() {}
}
