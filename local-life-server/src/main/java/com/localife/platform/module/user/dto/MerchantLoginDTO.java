package com.localife.platform.module.user.dto;

import lombok.Data;

/**
 * 商家登录请求
 */
@Data
public class MerchantLoginDTO {

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;
}
