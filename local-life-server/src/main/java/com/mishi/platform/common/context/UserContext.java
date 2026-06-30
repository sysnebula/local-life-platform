package com.mishi.platform.common.context;

/**
 * ThreadLocal 用户上下文
 */
public class UserContext {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<Integer> USER_TYPE = new ThreadLocal<>();
    private static final ThreadLocal<String> PHONE = new ThreadLocal<>();

    public static void setUserId(Long userId) { USER_ID.set(userId); }
    public static Long getUserId() { return USER_ID.get(); }

    public static void setUserType(Integer userType) { USER_TYPE.set(userType); }
    public static Integer getUserType() { return USER_TYPE.get(); }

    public static void setPhone(String phone) { PHONE.set(phone); }
    public static String getPhone() { return PHONE.get(); }

    private static final ThreadLocal<Long> SHOP_ID = new ThreadLocal<>();

    public static void setShopId(Long shopId) { SHOP_ID.set(shopId); }
    public static Long getShopId() { return SHOP_ID.get(); }

    public static void clear() {
        USER_ID.remove();
        USER_TYPE.remove();
        PHONE.remove();
        SHOP_ID.remove();
    }
}
