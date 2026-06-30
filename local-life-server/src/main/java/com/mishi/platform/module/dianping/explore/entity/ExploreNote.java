package com.mishi.platform.module.dianping.explore.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 探店笔记（简化版 — 去掉了点赞、评论等社交功能）
 */
@Data
@TableName("tb_explore_note")
public class ExploreNote {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long shopId;
    private Long userId;
    /** 关联订单ID */
    private Long orderId;
    /** 订单类型: 0=券订单 1=外卖订单 */
    private Integer orderType;
    private String title;
    private String images;
    private String content;
    private Integer status; // 1=已发布
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /** 以下为非数据库字段 */
    @TableField(exist = false)
    private String userName;
    @TableField(exist = false)
    private String shopName;
}
