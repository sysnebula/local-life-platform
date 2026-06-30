package com.localife.platform.module.dianping.voucher.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.localife.platform.common.context.UserContext;
import com.localife.platform.common.result.Result;
import com.localife.platform.module.dianping.voucher.entity.Voucher;
import com.localife.platform.module.dianping.voucher.entity.VoucherOrder;
import com.localife.platform.module.dianping.voucher.service.VoucherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户端-优惠券", description = "浏览券、购买券、秒杀")
@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
public class CustomerVoucherController {

    private final VoucherService voucherService;

    @Operation(summary = "查询店铺优惠券列表")
    @GetMapping("/voucher/list/{shopId}")
    public Result<Page<Voucher>> list(@PathVariable Long shopId,
                                      @RequestParam(defaultValue = "1") int page,
                                      @RequestParam(defaultValue = "10") int size) {
        return Result.success(voucherService.pageVouchers(shopId, null, page, size));
    }

    @Operation(summary = "购买普通优惠券")
    @PostMapping("/voucher/{voucherId}")
    public Result<VoucherOrder> buy(@PathVariable Long voucherId) {
        return Result.success(voucherService.buyVoucher(voucherId, UserContext.getUserId()));
    }

    @Operation(summary = "秒杀抢购")
    @PostMapping("/seckill/{voucherId}")
    public Result<Long> seckill(@PathVariable Long voucherId) {
        Long orderId = voucherService.seckillVoucher(voucherId, UserContext.getUserId());
        return Result.success(orderId);
    }

    @Operation(summary = "确认支付券订单")
    @PutMapping("/voucher/order/{id}/pay")
    public Result<Void> payOrder(@PathVariable Long id) {
        voucherService.payVoucherOrder(id, UserContext.getUserId());
        return Result.success();
    }

    @Operation(summary = "申请退款")
    @PutMapping("/voucher/order/{id}/refund")
    public Result<Void> refund(@PathVariable Long id) {
        voucherService.refundVoucherOrder(id, UserContext.getUserId());
        return Result.success();
    }

    @Operation(summary = "我的券订单")
    @GetMapping("/voucher/orders")
    public Result<Page<VoucherOrder>> myOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(voucherService.pageUserOrders(UserContext.getUserId(), page, size));
    }
}
