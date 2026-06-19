package com.localife.platform.module.user.controller;

import com.localife.platform.common.context.UserContext;
import com.localife.platform.common.result.Result;
import com.localife.platform.module.user.dto.UserLoginDTO;
import com.localife.platform.module.user.service.UserService;
import com.localife.platform.module.user.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户端 — 顾客接口
 */
@Tag(name = "用户端-用户", description = "顾客登录、注册、个人信息")
@RestController
@RequestMapping("/api/customer/user")
@RequiredArgsConstructor
public class CustomerUserController {

    private final UserService userService;

    @Operation(summary = "手机号+验证码登录")
    @PostMapping("/login")
    public Result<UserVO> login(@RequestBody UserLoginDTO dto) {
        return Result.success(userService.loginByPhone(dto));
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/me")
    public Result<UserVO> me() {
        return Result.success(userService.getCurrentUser(UserContext.getUserId()));
    }
}
