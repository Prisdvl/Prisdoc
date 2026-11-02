package com.javaee.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * 用户服务启动类，开启服务注册并扫描用户与公共组件。
 */
@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = {"com.javaee.user", "com.javaee.common"})
public class UserApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }

}
