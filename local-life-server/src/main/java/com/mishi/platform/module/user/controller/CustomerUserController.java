package com.mishi.platform.module.user.controller;

import com.mishi.platform.common.context.UserContext;
import com.mishi.platform.common.result.Result;
import com.mishi.platform.module.user.dto.UserLoginDTO;
import com.mishi.platform.module.user.dto.WxLoginDTO;
import com.mishi.platform.module.user.service.UserService;
import com.mishi.platform.module.user.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
    public Result<UserVO> login(@Valid @RequestBody UserLoginDTO dto) {
        return Result.success(userService.loginByPhone(dto));
    }

    @Operation(summary = "微信一键登录")
    @PostMapping("/wx-login")
    public Result<UserVO> wxLogin(@Valid @RequestBody WxLoginDTO dto) {
        return Result.success(userService.loginByWx(dto));
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/me")
    public Result<UserVO> me() {
        return Result.success(userService.getCurrentUser(UserContext.getUserId()));
    }

    @Operation(summary = "更新个人信息（昵称/头像）")
    @PutMapping("/me")
    public Result<Void> updateMe(@RequestBody Map<String, String> body) {
        userService.updateProfile(UserContext.getUserId(),
                body.get("nickName"), body.get("icon"));
        return Result.success();
    }
}
