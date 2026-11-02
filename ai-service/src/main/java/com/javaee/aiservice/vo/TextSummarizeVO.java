package com.javaee.aiservice.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文档摘要结果，包含摘要正文与压缩情况。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TextSummarizeVO {
    private String summary;
    private Integer originalLength;
    private Integer summaryLength;
    private Double compressionRatio;
}
