package com.mishi.platform.module.address.controller;

import com.mishi.platform.common.context.UserContext;
import com.mishi.platform.common.result.Result;
import com.mishi.platform.module.address.dto.AddressDTO;
import com.mishi.platform.module.address.entity.Address;
import com.mishi.platform.module.address.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户端-地址", description = "收货地址管理")
@RestController
@RequestMapping("/api/customer/address")
@RequiredArgsConstructor
public class CustomerAddressController {

    private final AddressService addressService;

    @Operation(summary = "我的地址列表")
    @GetMapping
    public Result<List<Address>> list() {
        return Result.success(addressService.listByUser(UserContext.getUserId()));
    }

    @Operation(summary = "新增地址")
    @PostMapping
    public Result<Address> add(@Valid @RequestBody AddressDTO dto) {
        return Result.success(addressService.add(UserContext.getUserId(), dto));
    }

    @Operation(summary = "编辑地址")
    @PutMapping("/{id}")
    public Result<Address> edit(@PathVariable Long id, @Valid @RequestBody AddressDTO dto) {
        return Result.success(addressService.edit(id, UserContext.getUserId(), dto));
    }

    @Operation(summary = "删除地址")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        addressService.remove(id, UserContext.getUserId());
        return Result.success();
    }

    @Operation(summary = "设为默认地址")
    @PutMapping("/{id}/default")
    public Result<Void> setDefault(@PathVariable Long id) {
        addressService.setDefault(id, UserContext.getUserId());
        return Result.success();
    }
}
