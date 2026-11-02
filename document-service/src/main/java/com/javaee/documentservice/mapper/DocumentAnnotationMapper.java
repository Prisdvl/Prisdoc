package com.javaee.documentservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.javaee.documentservice.entity.DocumentAnnotation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文档批注数据访问接口，负责按文档或行号检索批注。
 */
@Mapper
public interface DocumentAnnotationMapper extends BaseMapper<DocumentAnnotation> {

    /**
     * 查询文档下的全部有效批注。
     *
     * @param documentId 文档 ID
     * @return 批注实体列表
     */
    List<DocumentAnnotation> selectByDocumentId(@Param("documentId") String documentId);

    /**
     * 查询文档指定行的全部有效批注。
     *
     * @param documentId 文档 ID
     * @param lineNumber 行号
     * @return 该行的批注实体列表
     */
    List<DocumentAnnotation> selectByLineNumber(@Param("documentId") String documentId, @Param("lineNumber") Integer lineNumber);
}
