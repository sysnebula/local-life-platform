package com.localife.platform.module.user.dto;

import lombok.Data;

/**
 * 店员管理请求
 */
@Data
public class EmployeeDTO {

    private String name;
    private String phone;
    private String username;
    private String password;
    private String idNumber;
    private Integer sex;
}
