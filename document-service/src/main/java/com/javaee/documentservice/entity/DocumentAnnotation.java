package com.javaee.documentservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文档批注实体，对应 document_annotation 表的一条记录。
 */
@Data
@TableName("document_annotation")
public class DocumentAnnotation {

    /** 批注 ID，UUID 主键 */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /** 所属文档 ID */
    private String documentId;

    /** 批注作者用户 ID */
    private Long userId;

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
