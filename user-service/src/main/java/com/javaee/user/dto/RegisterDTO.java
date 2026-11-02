package com.javaee.user.dto;

import lombok.Data;

/**
 * 注册请求参数，包含用户名、密码、邮箱与手机号。
 *
 * @author qxk
 */
@Data
public class RegisterDTO {
    // 用户名
    private String username;

    // 密码
    private String password;

    // 邮箱
    private String email;

    // 手机号
    private String phone;
}
