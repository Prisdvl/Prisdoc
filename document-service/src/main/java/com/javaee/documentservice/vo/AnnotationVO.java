package com.javaee.documentservice.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 批注展示对象，向前端返回批注内容、定位区间与作者信息。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnnotationVO {
    /** 批注 ID */
    private String id;
    /** 所属文档 ID */
    private String documentId;
    /** 批注作者用户 ID */
    private Long userId;
    /** 批注作者用户名 */
    private String userName;
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
    /** 状态：active-有效，deleted-已删除 */
    private String status;
    /** 创建人标识 */
    private String createdBy;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;
}
