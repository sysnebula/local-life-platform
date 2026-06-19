package com.localife.platform.module.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.localife.platform.common.context.UserContext;
import com.localife.platform.common.result.Result;
import com.localife.platform.module.user.dto.EmployeeDTO;
import com.localife.platform.module.user.dto.MerchantLoginDTO;
import com.localife.platform.module.user.entity.User;
import com.localife.platform.module.user.service.UserService;
import com.localife.platform.module.user.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 商家端 — 商家登录 + 店员管理
 */
@Tag(name = "商家端-用户", description = "商家登录、店员CRUD")
@RestController
@RequestMapping("/api/merchant")
@RequiredArgsConstructor
public class MerchantUserController {

    private final UserService userService;

    @Operation(summary = "商家用户名密码登录")
    @PostMapping("/login")
    public Result<UserVO> login(@RequestBody MerchantLoginDTO dto) {
        return Result.success(userService.loginByPassword(dto));
    }

    @Operation(summary = "获取当前登录商家信息")
    @GetMapping("/me")
    public Result<UserVO> me() {
        return Result.success(userService.getCurrentUser(UserContext.getUserId()));
    }

    @Operation(summary = "店员分页查询")
    @GetMapping("/employees")
    public Result<Page<User>> pageEmployees(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name) {
        return Result.success(userService.pageEmployees(page, size, name));
    }

    @Operation(summary = "新增店员")
    @PostMapping("/employee")
    public Result<Void> addEmployee(@RequestBody EmployeeDTO dto) {
        userService.addEmployee(dto);
        return Result.success();
    }

    @Operation(summary = "编辑店员")
    @PutMapping("/employee/{id}")
    public Result<Void> updateEmployee(@PathVariable Long id, @RequestBody EmployeeDTO dto) {
        userService.updateEmployee(id, dto);
        return Result.success();
    }

    @Operation(summary = "启用/禁用店员")
    @PutMapping("/employee/{id}/status")
    public Result<Void> toggleStatus(@PathVariable Long id) {
        userService.toggleEmployeeStatus(id);
        return Result.success();
    }
}
