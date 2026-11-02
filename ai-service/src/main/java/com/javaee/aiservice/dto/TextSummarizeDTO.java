package com.javaee.aiservice.dto;

import lombok.Data;

/**
 * 文档摘要请求参数。
 */
@Data
public class TextSummarizeDTO {
    private String content;
    private Integer maxLength;
}
