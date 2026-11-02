package com.javaee.user.dto;

import lombok.Data;

/**
 * 登录请求参数，包含用户名与密码。
 *
 * @author qxk
 */
@Data
public class LoginDTO {
    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;
}
