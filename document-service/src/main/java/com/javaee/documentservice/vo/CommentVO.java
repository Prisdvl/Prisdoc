package com.javaee.documentservice.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论展示对象，顶层评论通过 replies 内嵌其回复列表。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentVO {
    /** 评论 ID */
    private String id;
    /** 所属文档 ID */
    private String documentId;
    /** 评论作者用户 ID */
    private Long userId;
    /** 评论作者用户名 */
    private String userName;
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
    /** 该评论下的回复列表，顶层评论查询时填充 */
    private List<CommentVO> replies;
}
