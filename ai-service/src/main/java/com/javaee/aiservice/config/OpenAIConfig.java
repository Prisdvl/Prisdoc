package com.javaee.aiservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * 外部模型调用的 HTTP 客户端配置。
 *
 * <p>注册 RestTemplate 供调用外部模型接口时复用。</p>
 */
@Configuration
public class OpenAIConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}