package com.localife.platform.module.shop.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.localife.platform.common.result.Result;
import com.localife.platform.module.shop.entity.Shop;
import com.localife.platform.module.shop.entity.ShopType;
import com.localife.platform.module.shop.service.ShopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户端-店铺", description = "浏览店铺、附近搜索")
@RestController
@RequestMapping("/api/customer/shop")
@RequiredArgsConstructor
public class CustomerShopController {

    private final ShopService shopService;

    @Operation(summary = "获取店铺详情（带缓存）")
    @GetMapping("/{id}")
    public Result<Shop> detail(@PathVariable Long id) {
        Shop shop = shopService.getByIdWithCache(id);
        return shop != null ? Result.success(shop) : Result.error("店铺不存在");
    }

    @Operation(summary = "按类型分页查询店铺")
    @GetMapping("/list")
    public Result<Page<Shop>> list(
            @RequestParam(required = false) Long typeId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(shopService.pageByType(typeId, page, size));
    }

    @Operation(summary = "搜索店铺")
    @GetMapping("/search")
    public Result<Page<Shop>> search(@RequestParam String keyword,
                                      @RequestParam(defaultValue = "1") int page,
                                      @RequestParam(defaultValue = "10") int size) {
        return Result.success(shopService.search(keyword, page, size));
    }

    @Operation(summary = "获取所有店铺类型")
    @GetMapping("/types")
    public Result<List<ShopType>> types() {
        return Result.success(shopService.listTypes());
    }
}
