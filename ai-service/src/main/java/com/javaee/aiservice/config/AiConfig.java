package com.javaee.aiservice.config;

import org.springframework.context.annotation.Configuration;

/**
 * AI 配置占位类，预留自定义 Spring AI 组件入口。
 *
 * <p>ChatClient 与 EmbeddingModel 依赖 Spring AI 自动配置，当前无需额外声明 Bean。</p>
 */
@Configuration
public class AiConfig {

    // Spring AI的ChatClient和EmbeddingModel通过自动配置完成
    // 如需自定义配置，可以在此添加@Bean方法
}
