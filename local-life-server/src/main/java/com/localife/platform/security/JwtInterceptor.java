package com.localife.platform.security;

import com.localife.platform.common.constant.JwtClaimsConstant;
import com.localife.platform.common.context.UserContext;
import com.localife.platform.common.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 令牌拦截器 —— 解析 Token 并注入用户上下文
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) {
        String token = extractToken(request);
        if (token == null || !jwtUtil.isValid(token)) {
            response.setStatus(401);
            return false;
        }
        Claims claims = jwtUtil.parseToken(token);
        Long userId = Long.valueOf(claims.getSubject());
        Integer userType = claims.get(JwtClaimsConstant.USER_TYPE, Integer.class);
        String phone = claims.get(JwtClaimsConstant.PHONE, String.class);

        UserContext.setUserId(userId);
        UserContext.setUserType(userType);
        UserContext.setPhone(phone);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        UserContext.clear();
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
