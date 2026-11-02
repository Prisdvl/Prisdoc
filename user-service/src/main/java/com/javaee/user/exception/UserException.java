package com.javaee.user.exception;

import lombok.Getter;

/**
 * 用户模块业务异常，携带错误码与提示语。
 *
 * @author qxk
 */
@Getter
public class UserException extends RuntimeException {

    private Integer code;

    private String message;

    public UserException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public UserException(String message) {
        super(message);
        this.message = message;
    }
}
