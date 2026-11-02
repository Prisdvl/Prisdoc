package com.javaee.common.exception;

import com.javaee.common.constant.ErrorCodeEnum;
import com.javaee.common.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器，把业务、令牌与参数校验异常统一转换为 Result 响应。
 *
 * <p>未被具体处理器匹配的异常走兜底处理，统一返回服务器内部错误。</p>
 *
 * @author qxk
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常。
     *
     * @param e 业务异常，携带错误码与提示语
     * @return 统一返回结果，包含业务错误码与提示语
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.error("业务异常: {}", e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    /**
     * 处理令牌异常。
     *
     * @param e 令牌异常，携带错误码与提示语
     * @return 统一返回结果，包含令牌错误码与提示语
     */
    @ExceptionHandler(TokenException.class)
    public Result<Void> handleTokenException(TokenException e) {
        log.error("令牌异常: {}", e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    /**
     * 处理 @Valid 触发的参数校验异常。
     *
     * @param e 参数校验异常，包含首个字段错误的提示语
     * @return 统一返回结果，错误码为请求参数错误
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "参数校验失败";
        log.error("参数校验异常: {}", message);
        return Result.fail(ErrorCodeEnum.BAD_REQUEST.getCode(), message);
    }

    /**
     * 处理参数绑定异常。
     *
     * @param e 参数绑定异常，包含首个字段错误的提示语
     * @return 统一返回结果，错误码为请求参数错误
     */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "参数绑定失败";
        log.error("参数绑定异常: {}", message);
        return Result.fail(ErrorCodeEnum.BAD_REQUEST.getCode(), message);
    }

    /**
     * 处理非法参数异常。
     *
     * @param e 非法参数异常，消息作为提示语返回
     * @return 统一返回结果，错误码为请求参数错误
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("非法参数异常: {}", e.getMessage());
        return Result.fail(ErrorCodeEnum.BAD_REQUEST.getCode(), e.getMessage());
    }

    /**
     * 处理所有未被具体处理器匹配的异常。
     *
     * @param e 未捕获的异常
     * @return 统一返回结果，错误码为服务器内部错误
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常: ", e);
        return Result.fail(ErrorCodeEnum.INTERNAL_SERVER_ERROR);
    }
}
