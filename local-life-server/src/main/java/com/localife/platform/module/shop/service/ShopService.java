package com.localife.platform.module.shop.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.localife.platform.module.shop.entity.Shop;
import com.localife.platform.module.shop.entity.ShopType;

import java.util.List;

public interface ShopService extends IService<Shop> {

    /**
     * 根据ID查询（带缓存）
     */
    Shop getByIdWithCache(Long id);

    /**
     * 更新店铺（删除缓存）
     */
    void updateShop(Shop shop);

    /**
     * 按类型分页查询
     */
    Page<Shop> pageByType(Long typeId, int page, int size);

    /**
     * 附近店铺搜索（Redis GEO）
     */
    List<Shop> queryNearby(Double longitude, Double latitude, Double radius);

    /**
     * 查询所有店铺类型
     */
    List<ShopType> listTypes();
}
