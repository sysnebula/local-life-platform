package com.localife.platform.module.dianping.voucher.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.localife.platform.common.result.Result;
import com.localife.platform.module.dianping.voucher.dto.VoucherDTO;
import com.localife.platform.module.dianping.voucher.entity.Voucher;
import com.localife.platform.module.dianping.voucher.service.VoucherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "商家端-优惠券", description = "创建/管理优惠券、转秒杀")
@RestController
@RequestMapping("/api/merchant/voucher")
@RequiredArgsConstructor
public class MerchantVoucherController {

    private final VoucherService voucherService;

    @Operation(summary = "分页查询优惠券")
    @GetMapping("/page")
    public Result<Page<Voucher>> page(
            @RequestParam(required = false) Long shopId,
            @RequestParam(required = false) Integer type,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(voucherService.pageVouchers(shopId, type, page, size));
    }

    @Operation(summary = "创建优惠券")
    @PostMapping
    public Result<Void> create(@RequestBody VoucherDTO dto) {
        voucherService.createVoucher(dto);
        return Result.success();
    }

    @Operation(summary = "编辑优惠券")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody VoucherDTO dto) {
        Voucher voucher = voucherService.getById(id);
        if (voucher == null) return Result.error("优惠券不存在");
        voucher.setTitle(dto.getTitle());
        voucher.setSubTitle(dto.getSubTitle());
        voucher.setRules(dto.getRules());
        voucher.setPayValue(dto.getPayValue());
        voucher.setActualValue(dto.getActualValue());
        voucherService.updateById(voucher);
        return Result.success();
    }

    @Operation(summary = "删除优惠券")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        voucherService.removeById(id);
        return Result.success();
    }

    @Operation(summary = "普通券转秒杀券")
    @PostMapping("/{id}/seckill")
    public Result<Void> convertToSeckill(@PathVariable Long id, @RequestBody VoucherDTO dto) {
        voucherService.convertToSeckill(id, dto);
        return Result.success();
    }
}
