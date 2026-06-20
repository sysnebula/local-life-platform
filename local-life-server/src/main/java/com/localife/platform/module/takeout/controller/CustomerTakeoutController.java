package com.localife.platform.module.takeout.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.localife.platform.common.context.UserContext;
import com.localife.platform.common.result.Result;
import com.localife.platform.module.takeout.cart.entity.CartItem;
import com.localife.platform.module.takeout.cart.service.CartService;
import com.localife.platform.module.takeout.category.entity.Category;
import com.localife.platform.module.takeout.category.mapper.CategoryMapper;
import com.localife.platform.module.takeout.dish.entity.Dish;
import com.localife.platform.module.takeout.dish.entity.DishFlavor;
import com.localife.platform.module.takeout.dish.mapper.DishFlavorMapper;
import com.localife.platform.module.takeout.dish.mapper.DishMapper;
import com.localife.platform.module.takeout.order.entity.Order;
import com.localife.platform.module.takeout.order.entity.OrderDetail;
import com.localife.platform.module.takeout.order.service.OrderService;
import com.localife.platform.module.takeout.setmeal.entity.Setmeal;
import com.localife.platform.module.takeout.setmeal.entity.SetmealDish;
import com.localife.platform.module.takeout.setmeal.mapper.SetmealDishMapper;
import com.localife.platform.module.takeout.setmeal.mapper.SetmealMapper;
import com.localife.platform.module.user.entity.AddressBook;
import com.localife.platform.module.user.mapper.AddressBookMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "用户端-外卖", description = "菜品浏览、购物车、下单、地址")
@RestController
@RequestMapping("/api/customer/takeout")
@RequiredArgsConstructor
public class CustomerTakeoutController {

    private final CategoryMapper categoryMapper;
    private final DishMapper dishMapper;
    private final DishFlavorMapper dishFlavorMapper;
    private final SetmealMapper setmealMapper;
    private final SetmealDishMapper setmealDishMapper;
    private final CartService cartService;
    private final OrderService orderService;
    private final AddressBookMapper addressBookMapper;

    // === 分类 ===
    @Operation(summary = "获取菜品/套餐分类")
    @GetMapping("/category/{shopId}")
    public Result<List<Category>> categories(@PathVariable Long shopId, @RequestParam(defaultValue = "1") Integer type) {
        return Result.success(categoryMapper.selectList(
                new LambdaQueryWrapper<Category>().eq(Category::getShopId, shopId).eq(Category::getType, type).orderByAsc(Category::getSort)));
    }

    // === 菜品 ===
    @Operation(summary = "按分类查菜品")
    @GetMapping("/dish/{categoryId}")
    public Result<List<Dish>> dishes(@PathVariable Long categoryId) {
        List<Dish> dishes = dishMapper.selectList(
                new LambdaQueryWrapper<Dish>().eq(Dish::getCategoryId, categoryId).eq(Dish::getStatus, 1));
        dishes.forEach(d -> d.setFlavors(dishFlavorMapper.selectList(
                new LambdaQueryWrapper<DishFlavor>().eq(DishFlavor::getDishId, d.getId()))));
        return Result.success(dishes);
    }

    // === 套餐 ===
    @Operation(summary = "按分类查套餐")
    @GetMapping("/setmeal/{categoryId}")
    public Result<List<Setmeal>> setmeals(@PathVariable Long categoryId) {
        List<Setmeal> setmeals = setmealMapper.selectList(
                new LambdaQueryWrapper<Setmeal>().eq(Setmeal::getCategoryId, categoryId).eq(Setmeal::getStatus, 1));
        setmeals.forEach(s -> s.setDishes(setmealDishMapper.selectList(
                new LambdaQueryWrapper<SetmealDish>().eq(SetmealDish::getSetmealId, s.getId()))));
        return Result.success(setmeals);
    }

    // === 购物车 ===
    @Operation(summary = "查看购物车")
    @GetMapping("/cart")
    public Result<List<CartItem>> cart() {
        return Result.success(cartService.list(UserContext.getUserId()));
    }

    @Operation(summary = "添加购物车")
    @PostMapping("/cart")
    public Result<Void> cartAdd(@RequestBody CartItem item) {
        cartService.add(UserContext.getUserId(), item);
        return Result.success();
    }

    @Operation(summary = "更新数量/删除")
    @PutMapping("/cart/{field}")
    public Result<Void> cartUpdate(@PathVariable String field, @RequestBody Map<String, Integer> body) {
        cartService.updateNumber(UserContext.getUserId(), field, body.getOrDefault("number", 0));
        return Result.success();
    }

    @Operation(summary = "清空购物车")
    @DeleteMapping("/cart")
    public Result<Void> cartClear() {
        cartService.clear(UserContext.getUserId());
        return Result.success();
    }

    // === 下单 ===
    @Operation(summary = "提交订单")
    @PostMapping("/order")
    public Result<Order> placeOrder(@RequestBody Map<String, Object> body) {
        Long shopId = Long.valueOf(body.get("shopId").toString());
        Long addressId = Long.valueOf(body.get("addressBookId").toString());
        String remark = (String) body.getOrDefault("remark", "");
        return Result.success(orderService.placeOrder(UserContext.getUserId(), shopId, addressId, remark));
    }

    @Operation(summary = "我的订单")
    @GetMapping("/orders")
    public Result<Page<Order>> myOrders(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size) {
        return Result.success(orderService.pageCustomerOrders(UserContext.getUserId(), page, size));
    }

    @Operation(summary = "订单详情")
    @GetMapping("/order/{id}")
    public Result<List<OrderDetail>> orderDetail(@PathVariable Long id) {
        return Result.success(orderService.getDetails(id));
    }

    @Operation(summary = "取消订单")
    @PutMapping("/order/{id}/cancel")
    public Result<Void> cancelOrder(@PathVariable Long id, @RequestBody Map<String, String> body) {
        orderService.cancel(id, body.getOrDefault("reason", ""));
        return Result.success();
    }

    @Operation(summary = "催单")
    @PostMapping("/order/{id}/remind")
    public Result<Void> remind(@PathVariable Long id) {
        orderService.remind(id);
        return Result.success();
    }

    // === 地址 ===
    @Operation(summary = "地址列表")
    @GetMapping("/address")
    public Result<List<AddressBook>> addresses() {
        return Result.success(addressBookMapper.selectList(
                new LambdaQueryWrapper<AddressBook>().eq(AddressBook::getUserId, UserContext.getUserId())));
    }

    @Operation(summary = "新增地址")
    @PostMapping("/address")
    public Result<Void> addressAdd(@RequestBody AddressBook addr) {
        addr.setUserId(UserContext.getUserId());
        addressBookMapper.insert(addr);
        return Result.success();
    }

    @Operation(summary = "编辑地址")
    @PutMapping("/address/{id}")
    public Result<Void> addressUpdate(@PathVariable Long id, @RequestBody AddressBook addr) {
        addr.setId(id);
        addr.setUserId(UserContext.getUserId());
        addressBookMapper.updateById(addr);
        return Result.success();
    }

    @Operation(summary = "删除地址")
    @DeleteMapping("/address/{id}")
    public Result<Void> addressDel(@PathVariable Long id) {
        addressBookMapper.deleteById(id);
        return Result.success();
    }

    @Operation(summary = "设为默认")
    @PutMapping("/address/{id}/default")
    public Result<Void> addressDefault(@PathVariable Long id) {
        AddressBook addr = addressBookMapper.selectById(id);
        if (addr != null) {
            // 取消其他默认
            addressBookMapper.selectList(
                    new LambdaQueryWrapper<AddressBook>().eq(AddressBook::getUserId, addr.getUserId())).forEach(a -> {
                a.setIsDefault(0);
                addressBookMapper.updateById(a);
            });
            addr.setIsDefault(1);
            addressBookMapper.updateById(addr);
        }
        return Result.success();
    }
}
