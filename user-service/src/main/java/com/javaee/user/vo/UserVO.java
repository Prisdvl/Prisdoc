package com.javaee.user.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户响应对象，向前端输出用户资料与账号状态。
 *
 * @author qxk
 */
@Data
public class UserVO {
    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 角色
     */
    private String role;

    /**
     * 状态（0:禁用, 1:启用）
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
