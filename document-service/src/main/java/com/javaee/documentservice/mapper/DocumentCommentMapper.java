package com.javaee.documentservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.javaee.documentservice.entity.DocumentComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文档评论数据访问接口，负责按文档、父评论检索评论。
 */
@Mapper
public interface DocumentCommentMapper extends BaseMapper<DocumentComment> {

    /**
     * 查询文档下的全部有效评论。
     *
     * @param documentId 文档 ID
     * @return 评论实体列表
     */
    List<DocumentComment> selectByDocumentId(@Param("documentId") String documentId);

    /**
     * 查询某条评论下的全部回复。
     *
     * @param parentId 父评论 ID
     * @return 回复实体列表
     */
    List<DocumentComment> selectByParentId(@Param("parentId") String parentId);

    /**
     * 查询文档下的顶层评论，不含回复。
     *
     * @param documentId 文档 ID
     * @return 顶层评论实体列表
     */
    List<DocumentComment> selectRootCommentsByDocumentId(@Param("documentId") String documentId);
}
