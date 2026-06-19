package com.localife.platform.module.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.localife.platform.module.user.dto.EmployeeDTO;
import com.localife.platform.module.user.dto.MerchantLoginDTO;
import com.localife.platform.module.user.dto.UserLoginDTO;
import com.localife.platform.module.user.entity.User;
import com.localife.platform.module.user.vo.UserVO;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {

    /**
     * 顾客 — 手机号+验证码登录
     */
    UserVO loginByPhone(UserLoginDTO dto);

    /**
     * 商家 — 用户名+密码登录
     */
    UserVO loginByPassword(MerchantLoginDTO dto);

    /**
     * 获取当前登录用户信息
     */
    UserVO getCurrentUser(Long userId);

    /**
     * 新增店员
     */
    void addEmployee(EmployeeDTO dto);

    /**
     * 编辑店员
     */
    void updateEmployee(Long id, EmployeeDTO dto);

    /**
     * 切换店员状态
     */
    void toggleEmployeeStatus(Long id);

    /**
     * 店员分页查询
     */
    Page<User> pageEmployees(int page, int size, String name);
}
