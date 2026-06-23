-- ============================================
--  本地生活平台 — 测试数据
--  密码: 商家 123456, 顾客验证码 123456
-- ============================================
USE local_life;

-- 店铺类型
INSERT INTO tb_shop_type (id, name, sort) VALUES
(1, '火锅', 1), (2, '快餐', 2), (3, '饮品甜品', 3),
(4, '川菜湘菜', 4), (5, '日料韩餐', 5), (6, '烧烤', 6);

-- 用户: 1商家 + 2顾客
INSERT INTO tb_user (id, phone, username, password, nick_name, name, sex, user_type, status, create_time) VALUES
(1, '13800001111', 'admin', '123456', '张店长', '张建国', 1, 1, 1, NOW());

INSERT INTO tb_user (id, phone, nick_name, sex, user_type, status, create_time) VALUES
(10, '13812345678', '美食猎人', 1, 0, 1, NOW()),
(11, '13987654321', '吃货小张', 2, 0, 1, NOW());

-- 店铺: 蜀九香火锅
INSERT INTO tb_shop (id, name, type_id, merchant_user_id, area, address, avg_price, sold, score, open_hours, phone, description, delivery_fee, min_order, status, create_time) VALUES
(1, '蜀九香火锅（三里屯店）', 1, 1, '朝阳区', '工体北路甲2号', 12800, 3286, 4.8, '10:00-22:00', '010-88886666', '京城知名重庆火锅，精选食材，正宗川渝味道。', 300, 2000, 1, NOW());

-- 菜品分类
INSERT INTO tb_category (id, shop_id, type, name, sort, status, create_time) VALUES
(1, 1, 1, '招牌锅底', 1, 1, NOW()),
(2, 1, 1, '精品荤菜', 2, 1, NOW()),
(3, 1, 1, '时令素菜', 3, 1, NOW()),
(4, 1, 2, '超值套餐', 1, 1, NOW());

-- 菜品
INSERT INTO tb_dish (id, shop_id, category_id, name, description, price, status, create_time) VALUES
(1, 1, 1, '九宫格牛油锅底', '正宗重庆牛油，麻辣鲜香', 6800, 1, NOW()),
(2, 1, 1, '番茄鸳鸯锅底', '一半牛油一半番茄，老少皆宜', 5800, 1, NOW()),
(3, 1, 2, '极品雪花肥牛', '澳洲M7和牛，入口即化', 8800, 1, NOW()),
(4, 1, 2, '手工虾滑', '鲜虾手打，Q弹爽滑', 4200, 1, NOW()),
(5, 1, 3, '时蔬拼盘', '当日时令蔬菜组合', 2800, 1, NOW());

-- 菜品口味
INSERT INTO tb_dish_flavor (id, dish_id, name, value) VALUES
(1, 1, '辣度', '微辣,中辣,特辣'),
(2, 3, '份量', '150g,300g');

-- 套餐
INSERT INTO tb_setmeal (id, shop_id, category_id, name, description, price, status, create_time) VALUES
(1, 1, 4, '双人火锅套餐', '锅底+肥牛+虾滑+素菜+饮品×2', 16800, 1, NOW());

INSERT INTO tb_setmeal_dish (id, setmeal_id, dish_id, name, price, copies) VALUES
(1, 1, 1, '九宫格牛油锅底', 6800, 1),
(2, 1, 3, '极品雪花肥牛', 8800, 1),
(3, 1, 4, '手工虾滑', 4200, 1),
(4, 1, 5, '时蔬拼盘', 2800, 1);

-- 优惠券
INSERT INTO tb_voucher (id, shop_id, title, sub_title, rules, pay_value, actual_value, type, status, create_time) VALUES
(1, 1, '100元代金券（秒杀）', '全场通用·每桌限1张', '{"useLimit":1,"validDays":30}', 990, 10000, 1, 1, NOW()),
(2, 1, '双人火锅套餐券', '原价¥198', '{"useLimit":1,"validDays":60}', 8800, 19800, 0, 1, NOW());

-- 秒杀券
INSERT INTO tb_seckill_voucher (voucher_id, stock, begin_time, end_time) VALUES
(1, 200, '2026-06-01 00:00:00', '2026-12-31 23:59:59');

-- 收货地址
INSERT INTO tb_address_book (id, user_id, consignee, sex, phone, province, city, district, detail, label, is_default) VALUES
(1, 10, '李先生', 1, '13812345678', '北京市', '朝阳区', '三里屯街道', '工体北路甲2号 3号楼1806', '家', 1);

-- 探店笔记
INSERT INTO tb_explore_note (id, shop_id, user_id, order_id, order_type, title, content, status, create_time) VALUES
(1, 1, 10, 1001, 0, '🔥 京城必打卡的重庆火锅！', '九宫格锅底真的正宗！牛油浓郁，辣度恰到好处。必点雪花肥牛和虾滑。', 1, NOW());
