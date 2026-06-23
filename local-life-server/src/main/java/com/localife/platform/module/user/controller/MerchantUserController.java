package com.localife.platform.module.user.controller;

import com.localife.platform.common.context.UserContext;
import com.localife.platform.common.result.Result;
import com.localife.platform.module.user.dto.MerchantLoginDTO;
import com.localife.platform.module.user.dto.MerchantRegisterDTO;
import com.localife.platform.module.user.service.UserService;
import com.localife.platform.module.user.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "商家端-用户", description = "商家登录、注册")
@RestController
@RequestMapping("/api/merchant")
@RequiredArgsConstructor
public class MerchantUserController {

    private final UserService userService;

    @Operation(summary = "商家用户名密码登录")
    @PostMapping("/login")
    public Result<UserVO> login(@Valid @RequestBody MerchantLoginDTO dto) {
        return Result.success(userService.loginByPassword(dto));
    }

    @Operation(summary = "商家注册（同时创建用户和店铺）")
    @PostMapping("/register")
    public Result<UserVO> register(@Valid @RequestBody MerchantRegisterDTO dto) {
        return Result.success(userService.register(dto));
    }

    @Operation(summary = "获取当前登录商家信息")
    @GetMapping("/me")
    public Result<UserVO> me() {
        return Result.success(userService.getCurrentUser(UserContext.getUserId()));
    }
}
