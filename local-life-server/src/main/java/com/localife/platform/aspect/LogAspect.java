package com.localife.platform.aspect;

import com.localife.platform.common.context.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * AOP 操作日志 — 记录每个接口的调用者、路径、入参、耗时
 */
@Slf4j
@Aspect
@Component
public class LogAspect {

    @Around("execution(* com.localife.platform.module..controller..*(..))")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String method = "";
        String uri = "";
        if (attrs != null) {
            HttpServletRequest req = attrs.getRequest();
            method = req.getMethod();
            uri = req.getRequestURI();
        }
        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Long userId = UserContext.getUserId();

        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Throwable e) {
            log.warn("[AOP] ERROR | {} {} | {} | user={} | {}ms | {}",
                    method, uri, className + "." + methodName, userId,
                    System.currentTimeMillis() - start, e.getMessage());
            throw e;
        }

        long cost = System.currentTimeMillis() - start;
        if (cost > 1000) {
            log.warn("[AOP] SLOW | {} {} | {} | user={} | {}ms",
                    method, uri, className + "." + methodName, userId, cost);
        } else {
            log.info("[AOP] {} {} | {} | user={} | {}ms",
                    method, uri, className + "." + methodName, userId, cost);
        }
        return result;
    }
}
