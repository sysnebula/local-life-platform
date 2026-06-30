package com.mishi.platform.module.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WxLoginDTO {

    @NotBlank(message = "微信code不能为空")
    private String code;
}
