-- ============================================
--  本地生活平台 — 建表 SQL
--  执行方式: mysql -u root -p < schema.sql
--  注意: 会先删除旧库再重建，数据会丢失！
-- ============================================

DROP DATABASE IF EXISTS local_life;
CREATE DATABASE local_life
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE local_life;

-- 统一用户表
CREATE TABLE IF NOT EXISTS tb_user
(
    id          BIGINT PRIMARY KEY,
    phone       VARCHAR(11) COMMENT '手机号',
    username    VARCHAR(32) UNIQUE COMMENT '商家登录用户名',
    password    VARCHAR(128) COMMENT 'BCrypt密码',
    nick_name   VARCHAR(32) COMMENT '昵称',
    name        VARCHAR(32) COMMENT '真实姓名',
    icon        VARCHAR(1024) COMMENT '头像URL',
    sex         TINYINT COMMENT '0=未知 1=男 2=女',
    user_type   TINYINT NOT NULL DEFAULT 0 COMMENT '0=顾客 1=商家/店长',
    status      TINYINT NOT NULL DEFAULT 1 COMMENT '0=禁用 1=启用',
    create_time DATETIME         DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME         DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX idx_phone_type (phone, user_type),
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
    sort INT DEFAULT 0 COMMENT '排序'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='店铺类型';

-- 优惠券表
CREATE TABLE IF NOT EXISTS tb_voucher
(
    id           BIGINT PRIMARY KEY,
    shop_id      BIGINT       NOT NULL COMMENT 'FK to tb_shop',
    title        VARCHAR(128) NOT NULL COMMENT '券标题',
    sub_title    VARCHAR(128) COMMENT '副标题',
    rules        TEXT COMMENT '使用规则(JSON)',
    pay_value    INT          NOT NULL COMMENT '支付金额(分)',
    actual_value INT          NOT NULL COMMENT '面值(分)',
    type         TINYINT DEFAULT 0 COMMENT '0=普通券 1=秒杀券',
    status       TINYINT DEFAULT 1 COMMENT '0=下架 1=上架',
    create_time  DATETIME,
    update_time  DATETIME,
    INDEX idx_shop (shop_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='优惠券';

-- 秒杀券表(1:1 tb_voucher)
CREATE TABLE IF NOT EXISTS tb_seckill_voucher
(
    voucher_id BIGINT PRIMARY KEY COMMENT '同tb_voucher.id',
    stock      INT      NOT NULL COMMENT '库存',
    begin_time DATETIME NOT NULL COMMENT '秒杀开始时间',
    end_time   DATETIME NOT NULL COMMENT '秒杀结束时间',
    FOREIGN KEY (voucher_id) REFERENCES tb_voucher (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='秒杀券';

-- 券订单表
CREATE TABLE IF NOT EXISTS tb_voucher_order
(
    id          BIGINT PRIMARY KEY,
    user_id     BIGINT NOT NULL COMMENT 'FK to tb_user',
    voucher_id  BIGINT NOT NULL COMMENT 'FK to tb_voucher',
    pay_type    TINYINT DEFAULT 0 COMMENT '0=微信支付',
    status      TINYINT DEFAULT 0 COMMENT '0=未支付 1=已支付 2=已退款 3=已核销',
    create_time DATETIME,
    pay_time    DATETIME,
    use_time    DATETIME,
    refund_time DATETIME,
    INDEX idx_user (user_id),
    INDEX idx_voucher (voucher_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='券订单';

-- ============================================
--  外卖模块
-- ============================================

-- 分类表
CREATE TABLE IF NOT EXISTS tb_category
(
    id          BIGINT PRIMARY KEY,
    shop_id     BIGINT      NOT NULL COMMENT 'FK to tb_shop',
    type        TINYINT COMMENT '1=菜品分类 2=套餐分类',
    name        VARCHAR(32) NOT NULL,
    sort        INT     DEFAULT 0,
    status      TINYINT DEFAULT 1,
    create_time DATETIME,
    update_time DATETIME,
    INDEX idx_shop (shop_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='分类';

-- 菜品表
CREATE TABLE IF NOT EXISTS tb_dish
(
    id          BIGINT PRIMARY KEY,
    shop_id     BIGINT      NOT NULL,
    category_id BIGINT      NOT NULL,
    name        VARCHAR(64) NOT NULL,
    image       VARCHAR(1024),
    description VARCHAR(256),
    price       INT         NOT NULL COMMENT '价格(分)',
    status      TINYINT DEFAULT 1 COMMENT '0=下架 1=上架',
    create_time DATETIME,
    update_time DATETIME,
    INDEX idx_shop_cat (shop_id, category_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='菜品';

-- 菜品口味表
CREATE TABLE IF NOT EXISTS tb_dish_flavor
(
    id      BIGINT PRIMARY KEY,
    dish_id BIGINT       NOT NULL,
    name    VARCHAR(32)  NOT NULL COMMENT '口味名称',
    value   VARCHAR(128) NOT NULL COMMENT '口味值列表',
    INDEX idx_dish (dish_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='菜品口味';

-- 套餐表
CREATE TABLE IF NOT EXISTS tb_setmeal
(
    id          BIGINT PRIMARY KEY,
    shop_id     BIGINT      NOT NULL,
    category_id BIGINT      NOT NULL,
    name        VARCHAR(64) NOT NULL,
    image       VARCHAR(1024),
    description VARCHAR(256),
    price       INT         NOT NULL COMMENT '价格(分)',
    status      TINYINT DEFAULT 1,
    create_time DATETIME,
    update_time DATETIME
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='套餐';

-- 套餐菜品关联表
CREATE TABLE IF NOT EXISTS tb_setmeal_dish
(
    id         BIGINT PRIMARY KEY,
    setmeal_id BIGINT NOT NULL,
    dish_id    BIGINT NOT NULL,
    name       VARCHAR(64),
    price      INT COMMENT '单价(分)',
    copies     INT DEFAULT 1,
    INDEX idx_setmeal (setmeal_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='套餐菜品';

-- 外卖订单表
CREATE TABLE IF NOT EXISTS tb_takeout_order
(
    id              BIGINT PRIMARY KEY,
    order_number    VARCHAR(64) NOT NULL UNIQUE,
    user_id         BIGINT      NOT NULL,
    shop_id         BIGINT      NOT NULL,
    address_book_id BIGINT,
    status          TINYINT DEFAULT 0 COMMENT '0=待接单 1=已接单 2=配送中 3=已完成 4=已取消',
    amount          INT         NOT NULL COMMENT '金额(分)',
    remark          VARCHAR(256),
    cancel_reason   VARCHAR(256),
    create_time     DATETIME,
    update_time     DATETIME,
    INDEX idx_user (user_id),
    INDEX idx_shop (shop_id),
    INDEX idx_status (status)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='外卖订单';

-- 订单明细表
CREATE TABLE IF NOT EXISTS tb_order_detail
(
    id         BIGINT PRIMARY KEY,
    order_id   BIGINT      NOT NULL,
    dish_id    BIGINT,
    setmeal_id BIGINT,
    name       VARCHAR(64) NOT NULL,
    image      VARCHAR(1024),
    flavor     VARCHAR(128),
    price      INT COMMENT '单价(分)',
    number     INT DEFAULT 1,
    INDEX idx_order (order_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='订单明细';

-- 地址簿表
CREATE TABLE IF NOT EXISTS tb_address_book
(
    id         BIGINT PRIMARY KEY,
    user_id    BIGINT      NOT NULL,
    consignee  VARCHAR(32) NOT NULL COMMENT '收货人',
    sex        TINYINT,
    phone      VARCHAR(11) NOT NULL,
    province   VARCHAR(32),
    city       VARCHAR(32),
    district   VARCHAR(32),
    detail     VARCHAR(256) COMMENT '详细地址',
    label      VARCHAR(32) COMMENT '家/公司等',
    is_default TINYINT DEFAULT 0,
    INDEX idx_user (user_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='地址簿';

-- 探店笔记表（关联订单 — 只有消费过才能写笔记）
CREATE TABLE IF NOT EXISTS tb_explore_note
(
    id          BIGINT PRIMARY KEY,
    shop_id     BIGINT       NOT NULL COMMENT 'FK to tb_shop',
    user_id     BIGINT       NOT NULL COMMENT 'FK to tb_user',
    order_id    BIGINT       NOT NULL COMMENT '关联订单ID (tb_takeout_order或tb_voucher_order)',
    order_type  TINYINT      NOT NULL DEFAULT 0 COMMENT '0=券订单 1=外卖订单',
    title       VARCHAR(128) NOT NULL,
    images      VARCHAR(2048) COMMENT '图片URL列表',
    content     TEXT,
    status      TINYINT DEFAULT 1 COMMENT '1=已发布',
    create_time DATETIME,
    update_time DATETIME,
    INDEX idx_shop (shop_id),
    INDEX idx_user (user_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='探店笔记';
