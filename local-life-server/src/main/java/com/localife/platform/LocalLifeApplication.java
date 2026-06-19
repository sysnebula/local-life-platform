package com.localife.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LocalLifeApplication {

    public static void main(String[] args) {
        SpringApplication.run(LocalLifeApplication.class, args);
        System.out.println("""

                ============================================
                  🏪 本地生活综合服务平台 启动成功！
                  Local Life Platform Started Successfully
                ============================================
                  Swagger UI: http://localhost:8080/swagger-ui.html
                ============================================
                """);
    }
}
