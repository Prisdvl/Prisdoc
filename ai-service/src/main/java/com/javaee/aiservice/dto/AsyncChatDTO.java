package com.javaee.aiservice.dto;

import lombok.Data;

/**
 * 异步通用模型调用的请求参数。
 */
@Data
public class AsyncChatDTO {
    private String prompt;
}
