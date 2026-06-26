package com.localife.platform.module.takeout.order.service;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.localife.platform.common.constant.OrderStatusEnum;
import com.localife.platform.common.exception.BusinessException;
import com.localife.platform.module.takeout.cart.entity.CartItem;
import com.localife.platform.module.takeout.cart.service.CartService;
import com.localife.platform.module.takeout.order.entity.Order;
import com.localife.platform.module.takeout.order.entity.OrderDetail;
import com.localife.platform.module.takeout.order.mapper.OrderDetailMapper;
import com.localife.platform.module.takeout.order.mapper.OrderMapper;
import com.localife.platform.websocket.OrderNotificationHandler;
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
    private final Snowflake snowflake = IdUtil.getSnowflake(1, 1);

    /**
     * 下单
     */
    @Transactional
    public Order placeOrder(Long userId, Long shopId, String remark) {
        List<CartItem> cartItems = cartService.list(userId);
        if (cartItems.isEmpty()) {
            throw new BusinessException("购物车为空");
        }

        // 计算总金额
        int total = cartItems.stream().mapToInt(i -> i.getPrice() * i.getNumber()).sum();

        Order order = new Order();
        order.setOrderNumber(String.valueOf(snowflake.nextId()));
        order.setUserId(userId);
        order.setShopId(shopId);
        order.setStatus(OrderStatusEnum.PENDING.getCode());
        order.setAmount(total);
        order.setRemark(remark);
        order.setCreateTime(LocalDateTime.now());
        save(order);

        // 订单明细
        for (CartItem item : cartItems) {
            OrderDetail detail = new OrderDetail();
            detail.setOrderId(order.getId());
            detail.setDishId(item.getDishId());
            detail.setSetmealId(item.getSetmealId());
            detail.setName(item.getName());
            detail.setImage(item.getImage());
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
