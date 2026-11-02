package com.javaee.common.config;

import feign.Logger;
import feign.Request;
import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Feign 全局配置，统一日志级别、超时时间与重试策略。
 *
 * @author qxk
 */
@Configuration
public class FeignGlobalConfig {

    /**
     * 配置 Feign 日志级别。
     *
     * @return 日志级别，固定为 FULL
     */
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    /**
     * 配置 Feign 连接与读取超时时间。
     *
     * @return 请求选项，连接超时 10 秒、读取超时 60 秒
     */
    @Bean
    public Request.Options feignOptions() {
        return new Request.Options(10, TimeUnit.SECONDS, 60, TimeUnit.SECONDS, true);
    }

    /**
     * 配置 Feign 重试策略。
     *
     * @return 重试策略，初始间隔 100 毫秒、最大间隔 1 秒、最多重试 3 次
     */
    @Bean
    public Retryer feignRetryer() {
        return new Retryer.Default(100, 1000, 3);
    }
}
