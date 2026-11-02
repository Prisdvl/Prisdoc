package com.javaee.documentservice.service;

import com.javaee.documentservice.dto.CommentCreateDTO;
import com.javaee.documentservice.vo.CommentVO;

import java.util.List;

/**
 * 评论服务接口，定义文档评论及其回复的增删查能力。
 *
 * <p>回复通过父评论 ID 关联；删除评论只把该条状态置为 deleted，不做物理删除。</p>
 */
public interface CommentService {

    /**
     * 创建评论或回复。
     *
     * @param dto 评论创建参数，携带父评论 ID 时表示回复
     * @param userId 发表用户 ID
     * @return 新建的评论信息
     */
    CommentVO createComment(CommentCreateDTO dto, Long userId);

    /**
     * 查询文档下的顶层评论，每条评论内嵌其回复列表。
     *
     * @param documentId 文档 ID
     * @return 顶层评论列表，回复挂在 {@code replies} 字段上
     */
    List<CommentVO> getCommentsByDocumentId(String documentId);

    /**
     * 按评论 ID 查询评论详情。
     *
     * @param id 评论 ID
     * @return 评论详情；评论不存在时抛出业务异常
     */
    CommentVO getCommentById(String id);

    /**
     * 删除评论。
     *
     * @param id 评论 ID
     * @param userId 操作用户 ID，需为评论作者
     */
    void deleteComment(String id, Long userId);

    /**
     * 查询某条评论的全部回复。
     *
     * @param parentId 父评论 ID
     * @return 该评论下的回复列表
     */
    List<CommentVO> getReplies(String parentId);
}
