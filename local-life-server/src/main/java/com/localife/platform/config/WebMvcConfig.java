package com.localife.platform.config;

import com.localife.platform.security.CustomerInterceptor;
import com.localife.platform.security.JwtInterceptor;
import com.localife.platform.security.MerchantInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置（CORS + 拦截器注册）
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;
    private final MerchantInterceptor merchantInterceptor;
    private final CustomerInterceptor customerInterceptor;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + System.getProperty("user.dir") + "/uploads/");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // JWT 拦截所有 /api/**
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/customer/user/login",
                        "/api/customer/user/wx-login",
                        "/api/customer/shop/types",
                        "/api/merchant/login",
                        "/api/merchant/register"
                );

        // 商家端角色校验
        registry.addInterceptor(merchantInterceptor)
                .addPathPatterns("/api/merchant/**")
                .excludePathPatterns("/api/merchant/login", "/api/merchant/register");

        // 用户端角色校验
        registry.addInterceptor(customerInterceptor)
                .addPathPatterns("/api/customer/**")
                .excludePathPatterns(
                        "/api/customer/user/login",
                        "/api/customer/user/wx-login",
                        "/api/customer/shop/types"
                );
    }
}
