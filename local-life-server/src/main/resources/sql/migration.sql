-- ============================================
--  本地生活平台 — 数据库迁移脚本
--  执行方式: mysql -u root -p < migration.sql
--  注意: 仅需执行一次，重复执行可能报错（可忽略）
-- ============================================

USE local_life;

-- 1. tb_user 新增微信 openid
ALTER TABLE tb_user ADD COLUMN openid VARCHAR(64) COMMENT '微信openid' AFTER phone;

-- 2. tb_shop 新增头像和配送时间
ALTER TABLE tb_shop ADD COLUMN image VARCHAR(1024) COMMENT '店铺头像' AFTER min_order;
ALTER TABLE tb_shop ADD COLUMN delivery_time INT DEFAULT 30 COMMENT '预计配送时间(分钟)' AFTER image;

-- 3. tb_takeout_order 新增支付状态和地址快照
ALTER TABLE tb_takeout_order ADD COLUMN paid TINYINT DEFAULT 0 COMMENT '0=未支付 1=已支付' AFTER status;
ALTER TABLE tb_takeout_order ADD COLUMN address_info VARCHAR(512) COMMENT '配送地址快照(JSON)' AFTER remark;

-- 4. 删除过于严格的全局唯一索引（普通券应允许重复购买）
ALTER TABLE tb_voucher_order DROP INDEX uk_user_voucher;

-- 5. 创建用户地址表
CREATE TABLE IF NOT EXISTS tb_address (
    id           BIGINT PRIMARY KEY,
    user_id      BIGINT       NOT NULL COMMENT 'FK to tb_user',
    contact_name VARCHAR(32)  NOT NULL COMMENT '联系人',
    phone        VARCHAR(20)  NOT NULL COMMENT '联系电话',
    province     VARCHAR(32)  COMMENT '省',
    city         VARCHAR(32)  COMMENT '市',
    district     VARCHAR(32)  COMMENT '区',
    detail       VARCHAR(255) NOT NULL COMMENT '详细地址',
    is_default   TINYINT DEFAULT 0 COMMENT '0=否 1=默认',
    create_time  DATETIME,
    update_time  DATETIME,
    INDEX idx_user (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='用户地址';

-- 6. 补充测试收货地址（顾客 id=10）
INSERT IGNORE INTO tb_address (id, user_id, contact_name, phone, province, city, district, detail, is_default, create_time)
VALUES (1, 10, '李先生', '13812345678', '北京市', '北京市', '朝阳区', '三里屯太古里南区3楼', 1, NOW());
