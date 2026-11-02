package com.javaee.common.model;

import com.javaee.common.constant.ErrorCodeEnum;

import java.io.Serializable;

/**
 * 全局统一返回结果，封装状态码、提示语、业务数据与时间戳。
 *
 * @author qxk
 */
public class Result<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    // 状态码
    private Integer code;

    // 消息
    private String message;

    // 数据
    private T data;

    // 时间戳
    private Long timestamp;

    // 构造方法
    public Result() {
        this.timestamp = System.currentTimeMillis();
    }

    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    // 静态方法
    /**
     * 构造携带业务数据的成功响应。
     *
     * @param data 业务数据
     * @param <T> 数据类型
     * @return 成功响应
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(ErrorCodeEnum.SUCCESS.getCode(), ErrorCodeEnum.SUCCESS.getMessage(), data);
    }

    /**
     * 构造不含业务数据的成功响应。
     *
     * @param <T> 数据类型
     * @return 成功响应，数据为空
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 构造携带自定义提示语与业务数据的成功响应。
     *
     * @param message 自定义提示语
     * @param data 业务数据
     * @param <T> 数据类型
     * @return 成功响应
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(ErrorCodeEnum.SUCCESS.getCode(), message, data);
    }

    /**
     * 构造指定状态码与提示语的失败响应。
     *
     * @param code 状态码
     * @param message 提示语
     * @param <T> 数据类型
     * @return 失败响应，数据为空
     */
    public static <T> Result<T> fail(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    /**
     * 按错误码枚举构造失败响应。
     *
     * @param errorCode 错误码枚举
     * @param <T> 数据类型
     * @return 失败响应，状态码与提示语取自错误码枚举
     */
    public static <T> Result<T> fail(ErrorCodeEnum errorCode) {
        return new Result<>(errorCode.getCode(), errorCode.getMessage(), null);
    }

    /**
     * 按错误码枚举与自定义提示语构造失败响应。
     *
     * @param errorCode 错误码枚举，提供状态码
     * @param message 自定义提示语
     * @param <T> 数据类型
     * @return 失败响应
     */
    public static <T> Result<T> fail(ErrorCodeEnum errorCode, String message) {
        return new Result<>(errorCode.getCode(), message, null);
    }

    /**
     * 按通用业务错误码构造失败响应。
     *
     * @param message 提示语
     * @param <T> 数据类型
     * @return 失败响应
     */
    public static <T> Result<T> fail(String message) {
        return new Result<>(ErrorCodeEnum.BUSINESS_ERROR.getCode(), message, null);
    }

    // getter and setter
    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    // toString
    @Override
    public String toString() {
        return "Result{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                ", timestamp=" + timestamp +
                '}';
    }
}

