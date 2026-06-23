package com.localife.platform.module.user.dto;

import lombok.Data;

/**
 * 商家注册请求（同时创建用户和店铺）
 */
@Data
public class MerchantRegisterDTO {

    // 用户信息
    private String username;
    private String password;
    private String phone;
    private String name;

    // 店铺信息
    private String shopName;
    private Long typeId;
    private String area;
    private String address;
    private String shopPhone;
    private String openHours;
    private String description;
}
