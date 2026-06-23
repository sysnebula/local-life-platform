package com.localife.platform.module.shop.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.localife.platform.common.constant.RedisConstants;
import com.localife.platform.common.context.UserContext;
import com.localife.platform.common.exception.BusinessException;
import com.localife.platform.module.shop.entity.Shop;
import com.localife.platform.module.shop.entity.ShopType;
import com.localife.platform.module.shop.mapper.ShopMapper;
import com.localife.platform.module.shop.mapper.ShopTypeMapper;
import com.localife.platform.module.shop.service.ShopService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements ShopService {

    private final StringRedisTemplate redisTemplate;
    private final ShopTypeMapper shopTypeMapper;

    // ==================== 查询（缓存策略） ====================

    @Override
    public Shop getByIdWithCache(Long id) {
        String key = RedisConstants.SHOP_CACHE_KEY + id;

        // 1. 查 Redis 缓存
        String shopJson = redisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(shopJson)) {
            Shop shop = JSONUtil.toBean(shopJson, Shop.class);
            if (shop.getId() == null) {
                // 空值标记，防止缓存穿透
                return null;
            }
            return shop;
        }

        // 2. 缓存未命中 — 互斥锁防缓存击穿
        Shop shop;
        String lockKey = "lock:shop:" + id;
        try {
            boolean locked = redisTemplate.opsForValue()
                    .setIfAbsent(lockKey, "1", RedisConstants.LOCK_TTL, TimeUnit.SECONDS);
            if (Boolean.TRUE.equals(locked)) {
                // 获得锁，查DB
                shop = getById(id);
                // 写入缓存（含空值保护）
                setShopCache(key, shop);
            } else {
                // 未获得锁，等待后重试
                Thread.sleep(50);
                return getByIdWithCache(id);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return getById(id);
        } finally {
            redisTemplate.delete(lockKey);
        }

        return shop;
    }

    // ==================== 更新 ====================

    @Override
    @Transactional
    public void updateShop(Shop shop) {
        // 店铺归属校验：只有自己的店铺才能编辑
        Long myShopId = UserContext.getShopId();
        if (myShopId != null && !myShopId.equals(shop.getId())) {
            throw new BusinessException(403, "无权操作此店铺");
        }
        shop.setUpdateTime(LocalDateTime.now());
        updateById(shop);
        // 删除缓存，下次查询时重建
        redisTemplate.delete(RedisConstants.SHOP_CACHE_KEY + shop.getId());
        // 更新 GEO
        if (shop.getLongitude() != null && shop.getLatitude() != null) {
            redisTemplate.opsForGeo().add(
                    RedisConstants.SHOP_GEO_KEY,
                    new Point(shop.getLongitude(), shop.getLatitude()),
                    shop.getId().toString()
            );
        }
    }

    // ==================== 分页 ====================

    @Override
    public Page<Shop> pageByType(Long typeId, int page, int size) {
        LambdaQueryWrapper<Shop> wrapper = new LambdaQueryWrapper<Shop>()
                .eq(typeId != null, Shop::getTypeId, typeId)
                .eq(Shop::getStatus, 1)
                .orderByDesc(Shop::getSold);
        return page(new Page<>(page, size), wrapper);
    }

    // ==================== GEO 附近搜索 ====================

    @Override
    public List<Shop> queryNearby(Double longitude, Double latitude, Double radius) {
        if (longitude == null || latitude == null) {
            throw new BusinessException("经纬度不能为空");
        }
        double searchRadius = (radius != null && radius > 0) ? radius : 5.0;

        // GEORADIUS 搜索
        Distance distance = new Distance(searchRadius, Metrics.KILOMETERS);
        Circle circle = new Circle(new Point(longitude, latitude), distance);
        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs
                .newGeoRadiusArgs()
                .includeDistance()
                .sortAscending()
                .limit(20);

        GeoResults<RedisGeoCommands.GeoLocation<String>> results =
                redisTemplate.opsForGeo().radius(RedisConstants.SHOP_GEO_KEY, circle, args);

        List<Shop> shops = new ArrayList<>();
        if (results != null) {
            for (GeoResult<RedisGeoCommands.GeoLocation<String>> r : results) {
                String shopIdStr = r.getContent().getName();
                Shop shop = getByIdWithCache(Long.valueOf(shopIdStr));
                if (shop != null) {
                    shop.setDistance(r.getDistance().getValue());
                    shops.add(shop);
                }
            }
        }
        return shops;
    }

    // ==================== 店铺类型 ====================

    @Override
    public List<ShopType> listTypes() {
        return shopTypeMapper.selectList(
                new LambdaQueryWrapper<ShopType>().orderByAsc(ShopType::getSort));
    }

    // ==================== 私有方法 ====================

    /**
     * 写入缓存 — 含空值保护和随机TTL（防缓存雪崩）
     */
    private void setShopCache(String key, Shop shop) {
        if (shop != null) {
            // 正常数据：随机 TTL 1800±300 秒，防止缓存雪崩
            long ttl = RedisConstants.SHOP_CACHE_TTL + RandomUtil.randomInt(-300, 300);
            redisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop), ttl, TimeUnit.SECONDS);
        } else {
            // 空值缓存 120 秒，防止缓存穿透
            redisTemplate.opsForValue().set(key, "{}", RedisConstants.CACHE_NULL_TTL, TimeUnit.SECONDS);
        }
    }
}
