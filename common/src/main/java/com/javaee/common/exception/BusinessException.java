package com.javaee.common.exception;

import com.javaee.common.constant.ErrorCodeEnum;
import lombok.Getter;

/**
 * 业务异常，承载错误码与提示语，用于表达可预期的业务失败。
 *
 * <p>默认使用通用业务错误码，也支持指定错误码或错误码枚举。</p>
 *
 * @author qxk
 */
@Getter
public class BusinessException extends RuntimeException {
    private Integer code;
    private String message;

    /**
     * 以错误消息构造业务异常，错误码取通用业务错误。
     *
     * @param message 错误消息
     */
    public BusinessException(String message) {
        super(message);
        this.code = ErrorCodeEnum.BUSINESS_ERROR.getCode();
        this.message = message;
    }

    /**
     * 以错误码与错误消息构造业务异常。
     *
     * @param code 错误码
     * @param message 错误消息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    /**
     * 以错误码枚举构造业务异常。
     *
     * @param errorCodeEnum 错误码枚举
     */
    public BusinessException(ErrorCodeEnum errorCodeEnum) {
        super(errorCodeEnum.getMessage());
        this.code = errorCodeEnum.getCode();
        this.message = errorCodeEnum.getMessage();
    }
}
