package com.javaee.aiservice.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 关键词提取结果。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KeywordExtractVO {
    private List<KeywordVO> keywords;
    private Integer totalCount;
}
