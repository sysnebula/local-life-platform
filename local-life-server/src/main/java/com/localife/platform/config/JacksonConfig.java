package com.localife.platform.config;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jackson 配置 — Long 序列化为 String，防止 JS 精度丢失
 */
@Configuration
public class JacksonConfig {

    /**
     * 将全局 Long 类型序列化为字符串
     * Snowflake 雪花ID 19位，超出 JS Number.MAX_SAFE_INTEGER (2^53-1)
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer longToStringCustomizer() {
        return builder -> {
            SimpleModule module = new SimpleModule();
            module.addSerializer(Long.class, ToStringSerializer.instance);
            module.addSerializer(long.class, ToStringSerializer.instance);
            builder.modulesToInstall(module);
        };
    }
}
