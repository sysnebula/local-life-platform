package com.localife.platform.module.takeout.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.localife.platform.common.result.Result;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "商家端-外卖", description = "分类/菜品/套餐CRUD、订单处理")
@RestController
@RequestMapping("/api/merchant/takeout")
@RequiredArgsConstructor
public class MerchantTakeoutController {

    private final CategoryMapper categoryMapper;
    private final DishMapper dishMapper;
    private final DishFlavorMapper dishFlavorMapper;
    private final SetmealMapper setmealMapper;
    private final SetmealDishMapper setmealDishMapper;
    private final OrderService orderService;

    // === 分类 ===
    @Operation(summary = "分类列表")
    @GetMapping("/category")
    public Result<List<Category>> categories(@RequestParam Long shopId, @RequestParam(defaultValue = "1") Integer type) {
        return Result.success(categoryMapper.selectList(
                new LambdaQueryWrapper<Category>().eq(Category::getShopId, shopId).eq(Category::getType, type).orderByAsc(Category::getSort)));
    }

    @PostMapping("/category")
    public Result<Void> categoryAdd(@RequestBody Category cat) {
        cat.setCreateTime(LocalDateTime.now());
        categoryMapper.insert(cat);
        return Result.success();
    }

    @PutMapping("/category")
    public Result<Void> categoryUpdate(@RequestBody Category cat) {
        categoryMapper.updateById(cat);
        return Result.success();
    }

    @DeleteMapping("/category/{id}")
    public Result<Void> categoryDel(@PathVariable Long id) {
        categoryMapper.deleteById(id);
        return Result.success();
    }

    // === 菜品 ===
    @Operation(summary = "菜品分页")
    @GetMapping("/dish/page")
    public Result<Page<Dish>> dishPage(@RequestParam Long shopId,
                                       @RequestParam(required = false) String name,
                                       @RequestParam(defaultValue = "1") int page,
                                       @RequestParam(defaultValue = "10") int size) {
        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<Dish>()
                .eq(Dish::getShopId, shopId)
                .like(cn.hutool.core.util.StrUtil.isNotBlank(name), Dish::getName, name)
                .orderByDesc(Dish::getCreateTime);
        Page<Dish> p = dishMapper.selectPage(new Page<>(page, size), wrapper);
        p.getRecords().forEach(d -> d.setFlavors(dishFlavorMapper.selectList(
                new LambdaQueryWrapper<DishFlavor>().eq(DishFlavor::getDishId, d.getId()))));
        return Result.success(p);
    }

    @PostMapping("/dish")
    public Result<Void> dishAdd(@RequestBody Dish dish) {
        dish.setCreateTime(LocalDateTime.now());
        dishMapper.insert(dish);
        if (dish.getFlavors() != null) dish.getFlavors().forEach(f -> {
            f.setDishId(dish.getId());
            dishFlavorMapper.insert(f);
        });
        return Result.success();
    }

    @PutMapping("/dish")
    public Result<Void> dishUpdate(@RequestBody Dish dish) {
        dishMapper.updateById(dish);
        dishFlavorMapper.delete(new LambdaQueryWrapper<DishFlavor>().eq(DishFlavor::getDishId, dish.getId()));
        if (dish.getFlavors() != null) dish.getFlavors().forEach(f -> {
            f.setDishId(dish.getId());
            dishFlavorMapper.insert(f);
        });
        return Result.success();
    }

    @DeleteMapping("/dish/{id}")
    public Result<Void> dishDel(@PathVariable Long id) {
        dishMapper.deleteById(id);
        dishFlavorMapper.delete(new LambdaQueryWrapper<DishFlavor>().eq(DishFlavor::getDishId, id));
        return Result.success();
    }

    @PutMapping("/dish/{id}/status")
    public Result<Void> dishToggle(@PathVariable Long id) {
        Dish d = dishMapper.selectById(id);
        if (d != null) {
            d.setStatus(d.getStatus() == 1 ? 0 : 1);
            dishMapper.updateById(d);
        }
        return Result.success();
    }

    // === 套餐 ===
    @Operation(summary = "套餐分页")
    @GetMapping("/setmeal/page")
    public Result<Page<Setmeal>> setmealPage(@RequestParam Long shopId,
                                              @RequestParam(required = false) String name,
                                              @RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "10") int size) {
        LambdaQueryWrapper<Setmeal> wrapper = new LambdaQueryWrapper<Setmeal>()
                .eq(Setmeal::getShopId, shopId)
                .like(cn.hutool.core.util.StrUtil.isNotBlank(name), Setmeal::getName, name)
                .orderByDesc(Setmeal::getCreateTime);
        Page<Setmeal> p = setmealMapper.selectPage(new Page<>(page, size), wrapper);
        p.getRecords().forEach(s -> s.setDishes(setmealDishMapper.selectList(
                new LambdaQueryWrapper<SetmealDish>().eq(SetmealDish::getSetmealId, s.getId()))));
        return Result.success(p);
    }

    @PostMapping("/setmeal")
    public Result<Void> setmealAdd(@RequestBody Setmeal s) {
        s.setCreateTime(LocalDateTime.now());
        setmealMapper.insert(s);
        if (s.getDishes() != null) s.getDishes().forEach(d -> {
            d.setSetmealId(s.getId());
            setmealDishMapper.insert(d);
        });
        return Result.success();
    }

    @PutMapping("/setmeal")
    public Result<Void> setmealUpdate(@RequestBody Setmeal s) {
        setmealMapper.updateById(s);
        setmealDishMapper.delete(new LambdaQueryWrapper<SetmealDish>().eq(SetmealDish::getSetmealId, s.getId()));
        if (s.getDishes() != null) s.getDishes().forEach(d -> {
            d.setSetmealId(s.getId());
            setmealDishMapper.insert(d);
        });
        return Result.success();
    }

    @DeleteMapping("/setmeal/{id}")
    public Result<Void> setmealDel(@PathVariable Long id) {
        setmealMapper.deleteById(id);
        setmealDishMapper.delete(new LambdaQueryWrapper<SetmealDish>().eq(SetmealDish::getSetmealId, id));
        return Result.success();
    }

    @PutMapping("/setmeal/{id}/status")
    public Result<Void> setmealToggle(@PathVariable Long id) {
        Setmeal s = setmealMapper.selectById(id);
        if (s != null) {
            s.setStatus(s.getStatus() == 1 ? 0 : 1);
            setmealMapper.updateById(s);
        }
        return Result.success();
    }

    // === 订单处理 ===
    @Operation(summary = "订单列表")
    @GetMapping("/order/page")
    public Result<Page<Order>> orderPage(@RequestParam(required = false) Long shopId, @RequestParam(required = false) Integer status,
                                         @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size) {
        return Result.success(orderService.pageMerchantOrders(shopId, status, page, size));
    }

    @GetMapping("/order/{id}")
    public Result<List<OrderDetail>> orderDetail(@PathVariable Long id) {
        return Result.success(orderService.getDetails(id));
    }

    @PutMapping("/order/{id}/accept")
    public Result<Void> accept(@PathVariable Long id) {
        orderService.accept(id);
        return Result.success();
    }

    @PutMapping("/order/{id}/deliver")
    public Result<Void> deliver(@PathVariable Long id) {
        orderService.deliver(id);
        return Result.success();
    }

    @PutMapping("/order/{id}/complete")
    public Result<Void> complete(@PathVariable Long id) {
        orderService.complete(id);
        return Result.success();
    }

    @PutMapping("/order/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        orderService.cancel(id, "商家取消");
        return Result.success();
    }
}
