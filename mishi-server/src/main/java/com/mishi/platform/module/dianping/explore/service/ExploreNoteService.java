package com.mishi.platform.module.dianping.explore.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mishi.platform.common.exception.BusinessException;
import com.mishi.platform.module.dianping.explore.entity.ExploreNote;
import com.mishi.platform.module.dianping.explore.mapper.ExploreNoteMapper;
import com.mishi.platform.module.dianping.voucher.entity.VoucherOrder;
import com.mishi.platform.module.dianping.voucher.mapper.VoucherOrderMapper;
import com.mishi.platform.module.shop.entity.Shop;
import com.mishi.platform.module.shop.mapper.ShopMapper;
import com.mishi.platform.module.takeout.order.entity.Order;
import com.mishi.platform.module.takeout.order.mapper.OrderMapper;
import com.mishi.platform.module.user.entity.User;
import com.mishi.platform.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExploreNoteService extends ServiceImpl<ExploreNoteMapper, ExploreNote> {

    private final VoucherOrderMapper voucherOrderMapper;
    private final OrderMapper orderMapper;
    private final ShopMapper shopMapper;
    private final UserMapper userMapper;

    public void publish(ExploreNote note) {
        if (note.getOrderId() == null) {
            throw new BusinessException("请选择关联的订单");
        }
        // 校验订单属于当前用户
        if (note.getOrderType() != null && note.getOrderType() == 1) {
            Order order = orderMapper.selectById(note.getOrderId());
            if (order == null || !order.getUserId().equals(note.getUserId()))
                throw new BusinessException("订单不存在");
        } else {
            VoucherOrder voucherOrder = voucherOrderMapper.selectById(note.getOrderId());
            if (voucherOrder == null || !voucherOrder.getUserId().equals(note.getUserId()))
                throw new BusinessException("订单不存在");
        }
        note.setStatus(1);
        note.setCreateTime(LocalDateTime.now());
        save(note);
    }

    public Page<ExploreNote> pageByShop(Long shopId, int page, int size) {
        Page<ExploreNote> result = page(new Page<>(page, size),
                new LambdaQueryWrapper<ExploreNote>()
                        .eq(ExploreNote::getShopId, shopId)
                        .eq(ExploreNote::getStatus, 1)
                        .orderByDesc(ExploreNote::getCreateTime));
        fillUserAndShop(result.getRecords());
        return result;
    }

    public Page<ExploreNote> pageByUser(Long userId, int page, int size) {
        Page<ExploreNote> result = page(new Page<>(page, size),
                new LambdaQueryWrapper<ExploreNote>()
                        .eq(ExploreNote::getUserId, userId)
                        .orderByDesc(ExploreNote::getCreateTime));
        fillUserAndShop(result.getRecords());
        return result;
    }

    private void fillUserAndShop(java.util.List<ExploreNote> notes) {
        if (notes.isEmpty()) return;
        Set<Long> userIds = notes.stream().map(ExploreNote::getUserId).collect(Collectors.toSet());
        Set<Long> shopIds = notes.stream().map(ExploreNote::getShopId).collect(Collectors.toSet());
        Map<Long, String> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u.getNickName() != null ? u.getNickName() : "用户"));
        Map<Long, String> shopMap = shopMapper.selectBatchIds(shopIds).stream()
                .collect(Collectors.toMap(Shop::getId, Shop::getName));
        notes.forEach(n -> {
            n.setUserName(userMap.getOrDefault(n.getUserId(), "用户"));
            n.setShopName(shopMap.getOrDefault(n.getShopId(), "未知店铺"));
        });
    }
}
