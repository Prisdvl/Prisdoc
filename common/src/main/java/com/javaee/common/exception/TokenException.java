package com.javaee.common.exception;

import com.javaee.common.constant.ErrorCodeEnum;
import lombok.Getter;

/**
 * 令牌异常，用于表达认证与授权环节的失败。
 *
 * <p>默认使用未授权错误码，也支持指定错误码或错误码枚举。</p>
 *
 * @author qxk
 */
@Getter
public class TokenException extends RuntimeException {
    private Integer code;
    private String message;

    /**
     * 以错误消息构造令牌异常，错误码取未授权。
     *
     * @param message 错误消息
     */
    public TokenException(String message) {
        super(message);
        this.code = ErrorCodeEnum.UNAUTHORIZED.getCode();
        this.message = message;
    }

    /**
     * 以错误码与错误消息构造令牌异常。
     *
     * @param code 错误码
     * @param message 错误消息
     */
    public TokenException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    /**
     * 以错误码枚举构造令牌异常。
     *
     * @param errorCodeEnum 错误码枚举
     */
    public TokenException(ErrorCodeEnum errorCodeEnum) {
        super(errorCodeEnum.getMessage());
        this.code = errorCodeEnum.getCode();
        this.message = errorCodeEnum.getMessage();
    }
}
