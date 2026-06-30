package com.localife.platform.module.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 统一用户表 — 融合顾客(C端) + 商家(B端)
 */
@Data
@TableName("tb_user")
public class User {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 手机号（顾客登录凭证）
     */
    private String phone;

    /**
     * 微信 openid（微信一键登录）
     */
    private String openid;

    /**
     * 用户名（商家登录凭证）
     */
    private String username;

    /**
     * BCrypt 加密密码
     */
    private String password;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 真实姓名（店员用）
     */
    private String name;

    /**
     * 头像 URL
     */
    private String icon;

    /**
     * 用户类型: 0=顾客, 1=商家
     */
    private Integer userType;

    /**
     * 状态: 0=禁用, 1=启用
     */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
