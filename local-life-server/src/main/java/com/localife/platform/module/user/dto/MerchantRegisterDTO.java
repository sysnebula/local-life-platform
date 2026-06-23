package com.localife.platform.module.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MerchantRegisterDTO {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
    private String phone;
    private String name;

    @NotBlank(message = "店铺名称不能为空")
    private String shopName;
    private Long typeId;
    private String area;
    private String address;
    private String shopPhone;
    private String openHours;
    private String description;
}
