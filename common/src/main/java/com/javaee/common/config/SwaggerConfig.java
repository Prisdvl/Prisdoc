package com.javaee.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 全局 Swagger 配置，定义对外 API 文档的标题、版本与说明。
 */
@Configuration
public class SwaggerConfig {

    /**
     * 构建 OpenAPI 文档元信息。
     *
     * @return OpenAPI 文档对象
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Prisdoc API")
                        .version("1.0")
                        .description("Prisdoc 系统的API文档，用于调试和测试接口"));
    }
}
