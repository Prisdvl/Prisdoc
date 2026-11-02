package com.javaee.documentservice.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Feign 请求拦截器配置，把当前请求的用户身份透传给内部服务调用。
 *
 * <p>仅在同一请求线程内生效；无请求上下文或请求头为空时不追加任何头，
 * 避免内部调用携带空的认证信息。</p>
 */
@Configuration
public class FeignAuthForwardConfig {

    /**
     * 创建透传认证头的 Feign 拦截器。
     *
     * @return 复制 Authorization 与 X-User-Id 请求头的拦截器
     */
    @Bean
    public RequestInterceptor authForwardInterceptor() {
        return template -> {
            if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) {
                return;
            }
            String authorization = attributes.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
            if (authorization != null && !authorization.isBlank()) {
                template.header(HttpHeaders.AUTHORIZATION, authorization);
            }
            String userId = attributes.getRequest().getHeader("X-User-Id");
            if (userId != null && !userId.isBlank()) {
                template.header("X-User-Id", userId);
            }
        };
    }
}
