package com.mishi.platform.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * springdoc-openapi 配置
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("觅食综合服务平台 API")
                        .version("1.0.0")
                        .description("整合到店团购 & 外卖到家两大业务场景")
                        .contact(new Contact()
                                .name("Local Life Platform")
                                .url("https://github.com/sysnebula/mishi")));
    }
}
