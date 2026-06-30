package com.localife.platform.module.takeout.order.service;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.localife.platform.common.constant.OrderStatusEnum;
import com.localife.platform.common.exception.BusinessException;
import com.localife.platform.module.address.entity.Address;
import com.localife.platform.module.address.mapper.AddressMapper;
import com.localife.platform.module.takeout.cart.entity.CartItem;
import com.localife.platform.module.takeout.cart.service.CartService;
import com.localife.platform.module.takeout.order.entity.Order;
import com.localife.platform.module.takeout.order.entity.OrderDetail;
import com.localife.platform.module.takeout.order.mapper.OrderDetailMapper;
import com.localife.platform.module.takeout.order.mapper.OrderMapper;
import com.localife.platform.module.shop.entity.Shop;
import com.localife.platform.module.shop.mapper.ShopMapper;
import com.localife.platform.websocket.OrderNotificationHandler;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService extends ServiceImpl<OrderMapper, Order> {

    private final OrderDetailMapper orderDetailMapper;
    private final CartService cartService;
    private final ShopMapper shopMapper;
    private final AddressMapper addressMapper;
    private final Snowflake snowflake = IdUtil.getSnowflake(1, 1);

    /**
     * 下单
     */
    @Transactional
    public Order placeOrder(Long userId, Long shopId, Long addressId, String remark) {
        List<CartItem> cartItems = cartService.list(userId);
        if (cartItems.isEmpty()) {
            throw new BusinessException("购物车为空");
        }

        // 查店铺配送信息
        Shop shop = shopMapper.selectById(shopId);
        if (shop == null) {
            throw new BusinessException("店铺不存在");
        }

        // 查配送地址
        Address address = null;
        if (addressId != null) {
            address = addressMapper.selectById(addressId);
        }
        if (address == null) {
            throw new BusinessException("请选择收货地址");
        }

        // 计算商品总金额
        int total = cartItems.stream().mapToInt(i -> i.getPrice() * i.getNumber()).sum();

        // 起送价校验
        int minOrder = shop.getMinOrder() != null ? shop.getMinOrder() : 0;
        if (total < minOrder) {
            throw new BusinessException("未达起送价 ¥" + (minOrder / 100.0) + "，还差 ¥" + ((minOrder - total) / 100.0));
        }

        // 加上配送费
        int deliveryFee = shop.getDeliveryFee() != null ? shop.getDeliveryFee() : 0;
        int finalAmount = total + deliveryFee;

        Order order = new Order();
        order.setOrderNumber(String.valueOf(snowflake.nextId()));
        order.setUserId(userId);
        order.setShopId(shopId);
        order.setStatus(OrderStatusEnum.PENDING.getCode());
        order.setPaid(0);
        order.setAmount(finalAmount);
        order.setRemark(remark);
        // 地址快照
        order.setAddressInfo(JSONUtil.toJsonStr(address));
        order.setCreateTime(LocalDateTime.now());
        save(order);

        // 订单明细
        for (CartItem item : cartItems) {
            OrderDetail detail = new OrderDetail();
            detail.setOrderId(order.getId());
            detail.setDishId(item.getDishId());
            detail.setSetmealId(item.getSetmealId());
            detail.setName(item.getName());
            detail.setFlavor(item.getFlavor());
            detail.setPrice(item.getPrice());
            detail.setNumber(item.getNumber());
            orderDetailMapper.insert(detail);
        }

        // 清空购物车
        cartService.clear(userId);

        // WebSocket 通知商家
        OrderNotificationHandler.notifyNewOrder(order.getShopId(),
                "新订单 #" + order.getOrderNumber() + " 金额 ¥" + total / 100.0);

        return order;
    }

    /**
     * 接单
     */
    @Transactional
    public void accept(Long orderId) {
        transition(orderId, OrderStatusEnum.PENDING, OrderStatusEnum.ACCEPTED);
    }

    /**
     * 派送
     */
    @Transactional
    public void deliver(Long orderId) {
        transition(orderId, OrderStatusEnum.ACCEPTED, OrderStatusEnum.DELIVERING);
    }

    /**
     * 完成
     */
    @Transactional
    public void complete(Long orderId) {
        transition(orderId, OrderStatusEnum.DELIVERING, OrderStatusEnum.COMPLETED);
    }

    /**
     * 取消
     */
    @Transactional
    public void cancel(Long orderId, String reason) {
        Order order = getById(orderId);
        if (order == null) throw new BusinessException("订单不存在");
        if (order.getStatus() > OrderStatusEnum.ACCEPTED.getCode()) {
            throw new BusinessException("已接单后不可取消，请联系商家");
        }
        order.setStatus(OrderStatusEnum.CANCELLED.getCode());
        order.setCancelReason(reason);
        updateById(order);
    }

    /**
     * 催单
     */
    public void remind(Long orderId) {
        Order order = getById(orderId);
        if (order != null) {
            OrderNotificationHandler.notifyNewOrder(order.getShopId(),
                    "⏰ 催单提醒！订单 #" + order.getOrderNumber());
        }
    }

    /**
     * 顾客订单列表
     */
    public Page<Order> pageCustomerOrders(Long userId, int page, int size) {
        return page(new Page<>(page, size),
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getUserId, userId)
                        .orderByDesc(Order::getCreateTime));
    }

    /**
     * 商家订单列表
     */
    public Page<Order> pageMerchantOrders(Long shopId, Integer status, int page, int size) {
        return page(new Page<>(page, size),
                new LambdaQueryWrapper<Order>()
                        .eq(shopId != null, Order::getShopId, shopId)
                        .eq(status != null, Order::getStatus, status)
                        .orderByDesc(Order::getCreateTime));
    }

    /**
     * 查询订单明细
     */
    public List<OrderDetail> getDetails(Long orderId) {
        return orderDetailMapper.selectList(
                new LambdaQueryWrapper<OrderDetail>().eq(OrderDetail::getOrderId, orderId));
    }

    /**
     * 确认支付外卖订单
     */
    @Transactional
    public void payOrder(Long orderId, Long userId) {
        Order order = getById(orderId);
        if (order == null) throw new BusinessException("订单不存在");
        if (!order.getUserId().equals(userId)) throw new BusinessException("订单不属于当前用户");
        if (order.getPaid() != null && order.getPaid() == 1) throw new BusinessException("订单已支付");
        order.setPaid(1);
        updateById(order);
    }

    private void transition(Long orderId, OrderStatusEnum from, OrderStatusEnum to) {
        Order order = getById(orderId);
        if (order == null) throw new BusinessException("订单不存在");
        if (!order.getStatus().equals(from.getCode())) {
            throw new BusinessException("订单状态异常");
        }
        order.setStatus(to.getCode());
        updateById(order);
    }
}
