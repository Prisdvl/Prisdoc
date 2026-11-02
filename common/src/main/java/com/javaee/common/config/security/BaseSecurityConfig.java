package com.javaee.common.config.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 安全配置基类，为各业务服务提供统一的无状态会话与 JWT 过滤器配置。
 *
 * <p>子类可复用基础过滤链，并按需追加各自的鉴权规则。</p>
 */
@Configuration
@EnableWebSecurity
public class BaseSecurityConfig {

    protected final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 注入 JWT 认证过滤器。
     *
     * @param jwtAuthenticationFilter JWT 认证过滤器
     */
    public BaseSecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * 应用基础安全配置：禁用 CSRF、启用无状态会话并注册 JWT 过滤器。
     *
     * @param http HttpSecurity 配置对象
     * @throws Exception 配置过滤链失败时抛出
     */
    protected void applyBaseSecurityConfig(HttpSecurity http) throws Exception {
        http
            // 禁用 CSRF 保护，适合 API 服务
            .csrf().disable()
            // 使用无状态会话管理
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            // 添加 JWT 认证过滤器
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    }

}
