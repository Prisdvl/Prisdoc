package com.javaee.documentservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文档评论实体，对应 document_comment 表的一条记录。
 */
@Data
@TableName("document_comment")
public class DocumentComment {

    /** 评论 ID，UUID 主键 */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /** 所属文档 ID */
    private String documentId;

    /** 评论作者用户 ID */
    private Long userId;

    /** 评论内容 */
    private String content;

    /** 父评论 ID，为空表示顶层评论 */
    private String parentId;

    /** 创建人标识 */
    private String createdBy;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 状态：active-有效，deleted-已删除 */
    private String status;
}
