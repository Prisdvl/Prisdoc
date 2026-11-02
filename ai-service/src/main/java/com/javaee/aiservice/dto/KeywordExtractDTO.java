package com.javaee.aiservice.dto;

import lombok.Data;

/**
 * 关键词提取请求参数。
 */
@Data
public class KeywordExtractDTO {
    private String content;
    private Integer count;
}
