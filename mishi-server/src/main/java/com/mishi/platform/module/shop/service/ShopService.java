package com.mishi.platform.module.shop.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mishi.platform.module.shop.entity.Shop;
import com.mishi.platform.module.shop.entity.ShopType;

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
     * 查询所有店铺类型
     */
    List<ShopType> listTypes();

    /** 根据商家用户ID查询所属店铺 */
    Shop getByMerchantUserId(Long userId);

    /** 搜索店铺（按名称模糊匹配） */
    Page<Shop> search(String keyword, int page, int size);
}
