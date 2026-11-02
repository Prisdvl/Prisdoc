package com.javaee.documentservice.dto;

import lombok.Data;

/**
 * 批注创建请求参数，描述批注的定位区间、类型与展示样式。
 */
@Data
public class AnnotationCreateDTO {
    /** 文档 ID，由接口路径参数回填 */
    private String documentId;
    /** 批注所在行号 */
    private Integer lineNumber;
    /** 批注起始字符偏移 */
    private Integer startOffset;
    /** 批注结束字符偏移 */
    private Integer endOffset;
    /** 批注类型：highlight-高亮，comment-注释，note-笔记 */
    private String annotationType;
    /** 批注内容 */
    private String content;
    /** 高亮颜色 */
    private String color;
}
