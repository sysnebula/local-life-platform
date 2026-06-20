-- ============================================
--  本地生活平台 — 建表 SQL
-- ============================================

CREATE DATABASE IF NOT EXISTS local_life
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE local_life;

-- 统一用户表
CREATE TABLE IF NOT EXISTS tb_user
(
    id          BIGINT PRIMARY KEY,
    phone       VARCHAR(11) UNIQUE COMMENT '手机号',
    openid      VARCHAR(45) UNIQUE COMMENT '微信openid',
    username    VARCHAR(32) UNIQUE COMMENT '商家登录用户名',
    password    VARCHAR(128) COMMENT 'BCrypt密码',
    nick_name   VARCHAR(32) COMMENT '昵称',
    name        VARCHAR(32) COMMENT '真实姓名',
    icon        VARCHAR(1024) COMMENT '头像URL',
    sex         TINYINT COMMENT '0=未知 1=男 2=女',
    id_number   VARCHAR(18) COMMENT '身份证号',
    user_type   TINYINT NOT NULL DEFAULT 0 COMMENT '0=顾客 1=商家',
    status      TINYINT NOT NULL DEFAULT 1 COMMENT '0=禁用 1=启用',
    create_time DATETIME         DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME         DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_phone (phone),
    INDEX idx_openid (openid),
    INDEX idx_user_type (user_type)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='统一用户表';

-- 店铺表
CREATE TABLE IF NOT EXISTS tb_shop
(
    id               BIGINT PRIMARY KEY,
    name             VARCHAR(128) NOT NULL COMMENT '店铺名称',
    type_id          BIGINT COMMENT 'FK to tb_shop_type',
    merchant_user_id BIGINT COMMENT 'FK to tb_user (商家)',
    images           VARCHAR(2048) COMMENT '图片URL列表',
    area             VARCHAR(32) COMMENT '区域',
    address          VARCHAR(255) COMMENT '详细地址',
    longitude        DOUBLE COMMENT '经度(GEO)',
    latitude         DOUBLE COMMENT '纬度(GEO)',
    avg_price        INT COMMENT '人均(分)',
    sold             INT           DEFAULT 0 COMMENT '总销量',
    score            DECIMAL(2, 1) DEFAULT 0 COMMENT '评分0-5',
    open_hours       VARCHAR(128) COMMENT '营业时间',
    phone            VARCHAR(20) COMMENT '联系电话',
    description      TEXT COMMENT '简介',
    delivery_fee     INT           DEFAULT 0 COMMENT '配送费(分)',
    min_order        INT           DEFAULT 0 COMMENT '起送价(分)',
    status           TINYINT       DEFAULT 1 COMMENT '0=休息 1=营业',
    create_time      DATETIME,
    update_time      DATETIME,
    INDEX idx_type (type_id),
    INDEX idx_merchant (merchant_user_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='店铺';

-- 店铺类型表
CREATE TABLE IF NOT EXISTS tb_shop_type
(
    id   BIGINT PRIMARY KEY,
    name VARCHAR(32) NOT NULL COMMENT '类型名称',
    icon VARCHAR(255) COMMENT '图标',
    sort INT DEFAULT 0 COMMENT '排序'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='店铺类型';
