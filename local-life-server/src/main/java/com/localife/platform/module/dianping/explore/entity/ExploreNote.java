package com.localife.platform.module.dianping.explore.entity;

import com.baomidou.mybatisplus.annotation.IdType;
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
    private String title;
    private String images;
    private String content;
    private Integer status; // 1=已发布
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
