package com.localife.platform.module.dianping.explore.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.localife.platform.common.exception.BusinessException;
import com.localife.platform.module.dianping.explore.entity.ExploreNote;
import com.localife.platform.module.dianping.explore.mapper.ExploreNoteMapper;
import com.localife.platform.module.dianping.voucher.entity.VoucherOrder;
import com.localife.platform.module.dianping.voucher.mapper.VoucherOrderMapper;
import com.localife.platform.module.takeout.order.entity.Order;
import com.localife.platform.module.takeout.order.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ExploreNoteService extends ServiceImpl<ExploreNoteMapper, ExploreNote> {

    private final VoucherOrderMapper voucherOrderMapper;
    private final OrderMapper orderMapper;

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
        return page(new Page<>(page, size),
                new LambdaQueryWrapper<ExploreNote>()
                        .eq(ExploreNote::getShopId, shopId)
                        .eq(ExploreNote::getStatus, 1)
                        .orderByDesc(ExploreNote::getCreateTime));
    }

    public Page<ExploreNote> pageByUser(Long userId, int page, int size) {
        return page(new Page<>(page, size),
                new LambdaQueryWrapper<ExploreNote>()
                        .eq(ExploreNote::getUserId, userId)
                        .orderByDesc(ExploreNote::getCreateTime));
    }
}
