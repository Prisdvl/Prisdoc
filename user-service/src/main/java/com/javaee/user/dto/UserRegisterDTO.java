package com.javaee.user.dto;

import lombok.Data;

/**
 * 用户注册数据传输对象，承载用户名、密码、邮箱与手机号。
 *
 * @author qxk
 */
@Data
public class UserRegisterDTO {

    private String username;

    private String password;

    private String email;

    private String phone;
}
