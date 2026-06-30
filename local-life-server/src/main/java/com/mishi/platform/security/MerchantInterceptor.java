package com.mishi.platform.security;

import com.mishi.platform.common.constant.UserTypeEnum;
import com.mishi.platform.common.context.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 商家端角色拦截器 —— 仅允许 MERCHANT 角色访问
 */
@Component
public class MerchantInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) {
        Integer userType = UserContext.getUserType();
        if (userType == null || !userType.equals(UserTypeEnum.MERCHANT.getCode())) {
            response.setStatus(403);
            return false;
        }
        return true;
    }
}
