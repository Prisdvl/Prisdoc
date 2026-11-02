package com.javaee.aiservice.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 单个关键词的词面、权重与类型。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KeywordVO {
    private String word;
    private Double score;
    private String type;
}
