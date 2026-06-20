# 🏪 本地生活综合服务平台

[![Java 17](https://img.shields.io/badge/Java-17-orange)](https://openjdk.org/)
[![Spring Boot 3.4](https://img.shields.io/badge/Spring%20Boot-3.4-brightgreen)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/license-MIT-blue)](LICENSE)

> 整合**到店团购**与**外卖到家**两大业务场景的本地生活综合服务平台。

## ✨ 核心功能

| 端                   | 功能                                             |
|---------------------|------------------------------------------------|
| 📱 **用户端** (微信小程序)  | 浏览店铺、GEO附近搜索、团购券/秒杀、外卖点餐、购物车、下单、探店笔记           |
| 🖥️ **商家端** (Web后台) | 店员管理、店铺编辑、菜品/套餐/分类管理、优惠券管理、订单处理（WebSocket实时通知） |

## 🛠️ 技术栈

| 组件   | 技术                                     |
|------|----------------------------------------|
| 框架   | Spring Boot 3.4 + MyBatis-Plus 3.5     |
| 数据库  | MySQL 8.x + Druid 连接池                  |
| 缓存   | Redis + Redisson（分布式锁、GEO、BloomFilter） |
| 消息队列 | RabbitMQ（秒杀异步下单削峰）                     |
| 认证   | JWT（jjwt 0.12）+ 拦截器链双角色鉴权              |
| 接口文档 | springdoc-openapi（OpenAPI 3.1）         |
| 实时通信 | WebSocket（订单来单提醒 + 催单）                 |
| 工具   | Hutool、Lombok                          |
| 对象存储 | 阿里云 OSS（图片上传）                          |

## 📦 项目结构

```
local-life-platform/
├── pom.xml                        # Parent POM
└── local-life-server/
    └── src/main/java/com/localife/platform/
        ├── common/                 # 公共：Result、异常、枚举、上下文、JwtUtil
        ├── config/                 # 配置：MyBatisPlus、Redis、Redisson、MQ、WebSocket、Swagger
        ├── security/               # 拦截器：JWT解析 + 双角色校验
        └── module/
            ├── user/               # 统一用户（顾客+商家+店员）
            ├── shop/               # 店铺（缓存三策略 + GEO）
            ├── dianping/
            │   ├── voucher/        # 优惠券 + 秒杀
            │   └── explore/        # 探店笔记
            └── takeout/
                ├── category/       # 分类管理
                ├── dish/           # 菜品管理
                ├── setmeal/        # 套餐管理
                ├── cart/           # 购物车（Redis Hash）
                └── order/          # 订单（状态流转 + WebSocket）
```

## 🚀 快速开始

### 环境要求

- Java 17+
- Maven 3.9+
- MySQL 8.0+
- Redis 7.0+
- RabbitMQ 3.x+

### 1. 配置

```bash
# 复制配置模板
cp local-life-server/src/main/resources/application-template.yml \
   local-life-server/src/main/resources/application-dev.yml
# 编辑 application-dev.yml 填入你的数据库/Redis/RabbitMQ 信息
```

### 2. 建库建表

```bash
mysql -u root -p < local-life-server/src/main/resources/sql/schema.sql
```

### 3. 启动

```bash
mvn clean package -DskipTests
java -jar local-life-server/target/local-life-server-1.0.0-SNAPSHOT.jar
```

### 4. API 文档

- Apifox 导入: `http://localhost:8080/v3/api-docs`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

## 🔑 关键技术难点

1. **缓存三问题**: 空值缓存防穿透 + SETNX互斥锁防击穿 + 随机TTL防雪崩
2. **秒杀高并发**: Redisson分布式锁 + Lua脚本原子扣库存 + RabbitMQ异步下单削峰 + Redis一人一单防重
3. **附近店铺搜索**: Redis GEO数据结构（GEOADD + GEORADIUS）
4. **统一认证体系**: JWT双角色（顾客+商家），拦截器链RBAC
5. **WebSocket实时通知**: 来单提醒 + 催单
6. **购物车**: Redis Hash实现，支持菜品/套餐混存

---

📝 *本项目整合自黑马程序员教学项目"黑马点评"与"苍穹外卖"，用于Java实习简历。*
