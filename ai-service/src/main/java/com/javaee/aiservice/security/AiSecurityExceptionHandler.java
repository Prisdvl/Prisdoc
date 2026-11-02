package com.javaee.aiservice.security;

import com.javaee.common.constant.ErrorCodeEnum;
import com.javaee.common.model.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 统一将 AI 服务的鉴权异常转换为标准响应。
 *
 * <p>只处理本服务包内抛出的 SecurityException，避免影响其他模块的异常处理。</p>
 */
@RestControllerAdvice(basePackages = "com.javaee.aiservice")
public class AiSecurityExceptionHandler {

    @ExceptionHandler(SecurityException.class)
    public Result<Void> handleSecurityException(SecurityException e) {
        return Result.fail(ErrorCodeEnum.FORBIDDEN.getCode(), e.getMessage());
    }
}
