package com.localife.platform.security;

import com.localife.platform.common.constant.UserTypeEnum;
import com.localife.platform.common.context.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 用户端角色拦截器 —— 仅允许 CUSTOMER 角色访问
 */
@Component
public class CustomerInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) {
        Integer userType = UserContext.getUserType();
        if (userType == null || !userType.equals(UserTypeEnum.CUSTOMER.getCode())) {
            response.setStatus(403);
            return false;
        }
        return true;
    }
}
