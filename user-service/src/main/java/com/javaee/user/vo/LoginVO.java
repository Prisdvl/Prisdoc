package com.javaee.user.vo;

import lombok.Data;

/**
 * 登录响应对象，返回访问令牌、刷新令牌与用户信息。
 *
 * @author qxk
 */
@Data
public class LoginVO {
    /**
     * 访问令牌
     */
    private String accessToken;

    /**
     * 刷新令牌
     */
    private String refreshToken;

    /**
     * 用户信息
     */
    private UserVO user;
}
