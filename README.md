# 🍜 觅食（MiShi）— 发现身边好味道

[![Java 17](https://img.shields.io/badge/Java-17-orange)](https://openjdk.org/)
[![Spring Boot 3.4](https://img.shields.io/badge/Spring%20Boot-3.4-brightgreen)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/license-MIT-blue)](LICENSE)

> 整合**外卖到家**与**到店团购**两大场景的本地美食服务平台。  
> 用户端微信小程序 + 商家端 Web 后台，后端 Spring Boot 3.4。

## ✨ 核心功能

| 端 | 功能 |
|---|------|
| 📱 **用户端** (微信小程序) | 浏览店铺、搜索、团购券/秒杀、外卖点餐、购物车、下单支付、探店笔记、地址管理 |
| 🖥️ **商家端** (Web 后台) | 店铺编辑、菜品/套餐/分类管理、优惠券管理、外卖订单流转、团购券核销（WebSocket 实时通知） |

## 🛠️ 技术栈

| 组件 | 技术 |
|------|------|
| 框架 | Spring Boot 3.4 + MyBatis-Plus 3.5 |
| 数据库 | MySQL 8.x + Druid 连接池 |
| 缓存 | Redis + Redisson（分布式锁） |
| 消息队列 | RabbitMQ（秒杀削峰） |
| 认证 | JWT + 三层拦截器链双角色鉴权 |
| 实时通信 | WebSocket（来单提醒 + 催单） |
| 接口文档 | springdoc-openapi |
| 工具 | Hutool、Lombok |
| 对象存储 | 阿里云 OSS |
| 用户端前端 | UniApp (Vue 3) → 微信小程序 |
| 商家端前端 | Vue 3 + Vite + Element Plus |

## 📦 项目结构

```
mishi/
├── pom.xml                          # Parent POM
├── local-life-server/               # Spring Boot 后端
│   └── src/main/java/com/mishi/platform/
│       ├── common/                  # Result、异常、枚举、上下文、JwtUtil
│       ├── config/                  # MyBatisPlus、Redis、Redisson、MQ、WebSocket、Swagger
│       ├── security/                # JWT 三层拦截器
│       └── module/
│           ├── user/                # 统一用户（顾客 + 商家）
│           ├── shop/                # 店铺（缓存三策略）
│           ├── address/             # 收货地址
│           ├── dianping/
│           │   ├── voucher/         # 优惠券 + 秒杀
│           │   └── explore/         # 探店笔记
│           └── takeout/
│               ├── category/        # 分类
│               ├── dish/            # 菜品
│               ├── setmeal/         # 套餐
│               ├── cart/            # 购物车（Redis Hash）
│               └── order/           # 外卖订单（状态流转 + WebSocket）
├── local-life-admin/                # Vue 3 商家后台
└── local-life-uniapp/               # UniApp 用户端
```

## 🚀 快速开始

### 环境要求

- Java 17+ / Maven 3.9+ / MySQL 8.0+ / Redis 7.0+ / RabbitMQ 3.x+

### 1. 配置

```bash
# 复制配置模板
cp local-life-server/src/main/resources/application-template.yml \
   local-life-server/src/main/resources/application-dev.yml
# 编辑 application-dev.yml 填入数据库/Redis/RabbitMQ 信息
```

### 2. 建库建表 + 测试数据

```bash
mysql -u root -p < local-life-server/src/main/resources/sql/schema.sql
mysql -u root -p < local-life-server/src/main/resources/sql/data.sql
```

### 3. 启动后端

```bash
cd local-life-server
mvn spring-boot:run
```

### 4. 启动前端

```bash
# 商家后台
cd local-life-admin && npm install && npm run dev

# 用户端小程序（编译后导入微信开发者工具）
cd local-life-uniapp && npm install && npm run dev:mp-weixin
# → 微信开发者工具导入 dist/dev/mp-weixin 目录
```

### 5. API 文档

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## 🔑 关键技术点

1. **秒杀防超卖**: Redis 一人一单 + DB 兜底 + Redisson 分布式锁 + Lua 原子扣库存 + 条件 SQL 防负数
2. **缓存三策略**: 空值防穿透 + SETNX 防击穿 + 随机 TTL 防雪崩
3. **统一认证**: JWT 三层拦截器链（Token 解析 → 顾客角色 → 商家角色）+ 微信一键登录
4. **WebSocket 实时通知**: 来单提醒 + 催单推送
5. **购物车**: Redis Hash，按店铺分组，支持菜品/套餐混存
6. **优惠券双体系**: 普通券乐观锁 + 秒杀券 Redis+Lua，库存独立互不干扰
7. **地址快照**: 下单时地址 JSON 序列化存入订单，历史订单不受后续地址变更影响

## 🧪 测试账号

| 角色 | 账号 | 密码/验证码 |
|------|------|-----------|
| 商家 | admin | 123456 |
| 顾客 | 13812345678 | 123456（验证码） |
