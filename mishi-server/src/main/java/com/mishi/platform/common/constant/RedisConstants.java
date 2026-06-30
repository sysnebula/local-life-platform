package com.mishi.platform.common.constant;

/**
 * Redis Key 常量
 */
public final class RedisConstants {

    /** 店铺缓存 */
    public static final String SHOP_CACHE_KEY = "shop:cache:";
    /** 店铺GEO */
    /** 秒杀券库存 */
    public static final String SECKILL_STOCK_KEY = "voucher:stock:";
    /** 秒杀一人一单防重 */
    public static final String SECKILL_ORDER_KEY = "seckill:order:user:";
    /** 购物车 */
    public static final String CART_KEY = "cart:";
    /** 分布式锁 */
    public static final String LOCK_SECKILL_KEY = "lock:seckill:";
    /** 短信验证码 */
    public static final String SMS_CODE_KEY = "sms:code:";
    /** 店铺缓存TTL(秒) */
    public static final long SHOP_CACHE_TTL = 1800;
    /** 空值缓存TTL(秒) */
    public static final long CACHE_NULL_TTL = 120;
    /** 分布式锁TTL(秒) */
    public static final long LOCK_TTL = 10;

    private RedisConstants() {}
}
