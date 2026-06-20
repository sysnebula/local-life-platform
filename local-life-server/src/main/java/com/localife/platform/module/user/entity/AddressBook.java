package com.localife.platform.module.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("tb_address_book")
public class AddressBook {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long userId;
    private String consignee;
    private Integer sex;
    private String phone;
    private String province;
    private String city;
    private String district;
    private String detail;
    private String label; // 家、公司等
    private Integer isDefault;
}
