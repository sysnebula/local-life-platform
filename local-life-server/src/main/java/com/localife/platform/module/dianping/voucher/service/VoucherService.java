package com.localife.platform.module.dianping.voucher.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.localife.platform.module.dianping.voucher.dto.VoucherDTO;
import com.localife.platform.module.dianping.voucher.entity.Voucher;
import com.localife.platform.module.dianping.voucher.entity.VoucherOrder;

public interface VoucherService extends IService<Voucher> {

    /**
     * 普通券购买（乐观锁）
     */
    VoucherOrder buyVoucher(Long voucherId, Long userId);

    /**
     * 秒杀券抢购
     */
    Long seckillVoucher(Long voucherId, Long userId);

    /**
     * 商家创建优惠券
     */
    void createVoucher(VoucherDTO dto);

    /**
     * 商家转秒杀券
     */
    void convertToSeckill(Long voucherId, VoucherDTO dto);

    /**
     * 分页查询优惠券
     */
    Page<Voucher> pageVouchers(Long shopId, Integer type, int page, int size);

    /**
     * 查询用户券订单
     */
    Page<VoucherOrder> pageUserOrders(Long userId, int page, int size);
}
