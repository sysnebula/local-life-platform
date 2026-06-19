package com.localife.platform.module.user.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 用户信息响应
 */
@Data
@Builder
public class UserVO {

    private Long id;
    private String phone;
    private String nickName;
    private String name;
    private String icon;
    private Integer sex;
    private Integer userType;
    private String token;
}
