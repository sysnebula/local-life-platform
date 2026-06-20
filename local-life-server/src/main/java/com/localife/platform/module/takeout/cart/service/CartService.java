package com.localife.platform.module.takeout.cart.service;

import cn.hutool.json.JSONUtil;
import com.localife.platform.common.constant.RedisConstants;
import com.localife.platform.module.takeout.cart.entity.CartItem;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 购物车 — Redis Hash 实现
 * Key: cart:{userId}
 * Field: dishId 或 setmealId
 * Value: CartItem JSON
 */
@Service
@RequiredArgsConstructor
public class CartService {

    private final StringRedisTemplate redisTemplate;

    public void add(Long userId, CartItem item) {
        String key = RedisConstants.CART_KEY + userId;
        String field = item.getDishId() != null
                ? "dish_" + item.getDishId()
                : "setmeal_" + item.getSetmealId();

        // 已存在则数量+1
        String exist = (String) redisTemplate.opsForHash().get(key, field);
        if (exist != null) {
            CartItem existItem = JSONUtil.toBean(exist, CartItem.class);
            existItem.setNumber(existItem.getNumber() + 1);
            redisTemplate.opsForHash().put(key, field, JSONUtil.toJsonStr(existItem));
        } else {
            item.setNumber(1);
            redisTemplate.opsForHash().put(key, field, JSONUtil.toJsonStr(item));
        }
    }

    public List<CartItem> list(Long userId) {
        String key = RedisConstants.CART_KEY + userId;
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        List<CartItem> items = new ArrayList<>();
        for (Object value : entries.values()) {
            items.add(JSONUtil.toBean((String) value, CartItem.class));
        }
        return items;
    }

    public void updateNumber(Long userId, String field, int number) {
        String key = RedisConstants.CART_KEY + userId;
        String exist = (String) redisTemplate.opsForHash().get(key, field);
        if (exist != null) {
            CartItem item = JSONUtil.toBean(exist, CartItem.class);
            if (number <= 0) {
                redisTemplate.opsForHash().delete(key, field);
            } else {
                item.setNumber(number);
                redisTemplate.opsForHash().put(key, field, JSONUtil.toJsonStr(item));
            }
        }
    }

    public void remove(Long userId, String field) {
        redisTemplate.opsForHash().delete(RedisConstants.CART_KEY + userId, field);
    }

    public void clear(Long userId) {
        redisTemplate.delete(RedisConstants.CART_KEY + userId);
    }
}
