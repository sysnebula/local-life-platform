package com.localife.platform.module.shop.controller;

import com.localife.platform.common.context.UserContext;
import com.localife.platform.common.result.Result;
import com.localife.platform.module.shop.entity.Shop;
import com.localife.platform.module.shop.service.ShopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "商家端-店铺", description = "管理店铺信息")
@RestController
@RequestMapping("/api/merchant/shop")
@RequiredArgsConstructor
public class MerchantShopController {

    private final ShopService shopService;

    @Operation(summary = "编辑店铺信息")
    @PutMapping
    public Result<Void> update(@RequestBody Shop shop) {
        shopService.updateShop(shop);
        return Result.success();
    }

    @Operation(summary = "获取当前登录商家的店铺")
    @GetMapping("/my")
    public Result<Shop> myShop() {
        Shop shop = shopService.getByMerchantUserId(UserContext.getUserId());
        return shop != null ? Result.success(shop) : Result.error("店铺不存在");
    }

    @Operation(summary = "切换营业状态")
    @PutMapping("/{id}/status")
    public Result<Void> toggleStatus(@PathVariable Long id) {
        Shop shop = shopService.getById(id);
        if (shop == null) return Result.error("店铺不存在");
        shop.setStatus(shop.getStatus() == 1 ? 0 : 1);
        shopService.updateShop(shop);
        return Result.success();
    }
}
