package com.javaee.fileservice.config;

import com.javaee.common.config.security.BaseSecurityConfig;
import com.javaee.common.config.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 文件服务安全配置，注册密码加密器并放行文档、文件接口与健康检查等路径。
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends BaseSecurityConfig {

    /**
     * 构造函数注入 JWT 认证过滤器。
     *
     * @param jwtAuthenticationFilter JWT 认证过滤器
     */
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        super(jwtAuthenticationFilter);
    }

    /**
     * 提供 BCrypt 密码加密器。
     *
     * @return BCrypt 密码加密器实例
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 单独暴露 BCryptPasswordEncoder 实例，便于按具体类型注入。
     *
     * @return BCrypt 密码加密器实例
     */
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 配置安全过滤链，声明各路径的访问规则。
     *
     * @param http Spring Security 的 HTTP 安全构建器
     * @return 构建完成的安全过滤链
     * @throws Exception 构建过滤链失败时抛出
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // 应用基础安全配置
        applyBaseSecurityConfig(http);
        
        http
            // 授权配置
            .authorizeHttpRequests(authorize -> authorize
                // 确保 Swagger 相关路径的匹配规则在最前面
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/v3/api-docs").permitAll()
                // 允许文件服务相关接口访问
                .requestMatchers("/api/files/**").permitAll()
                // 允许静态资源访问
                .requestMatchers("/static/**", "/public/**").permitAll()
                // 允许健康检查等端点访问
                .requestMatchers("/actuator/**").permitAll()
                // 允许错误处理端点访问
                .requestMatchers("/error").permitAll()
                // 其他接口需要认证
                .anyRequest().authenticated()
            );

        return http.build();
    }
}
