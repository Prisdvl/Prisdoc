package com.javaee.documentservice.dto;

import lombok.Data;

/**
 * 评论创建请求参数，携带父评论 ID 时表示对已有评论的回复。
 */
@Data
public class CommentCreateDTO {
    /** 文档 ID，由接口路径参数回填 */
    private String documentId;
    /** 评论内容 */
    private String content;
    /** 父评论 ID，为空表示顶层评论 */
    private String parentId;
}
