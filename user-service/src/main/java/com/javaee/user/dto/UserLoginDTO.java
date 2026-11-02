package com.javaee.user.dto;

import lombok.Data;

/**
 * 用户登录数据传输对象，承载用户名与密码。
 *
 * @author qxk
 */
@Data
public class UserLoginDTO {

    private String username;

    private String password;
}
