package com.localife.platform.module.user.dto;

import lombok.Data;

/**
 * 顾客登录请求
 */
@Data
public class UserLoginDTO {

    /**
     * 手机号
     */
    private String phone;

    /**
     * 短信验证码
     */
    private String code;
}
