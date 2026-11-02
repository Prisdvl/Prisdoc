package com.javaee.documentservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.javaee.documentservice.entity.DocumentVersion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文档版本数据访问接口，提供版本列表与指定版本查询。
 */
@Mapper
public interface DocumentVersionMapper extends BaseMapper<DocumentVersion> {

    /**
     * 查询文档的全部历史版本。
     *
     * @param documentId 文档 ID
     * @return 版本列表，按版本号降序排列
     */
    List<DocumentVersion> selectByDocumentId(@Param("documentId") String documentId);

    /**
     * 查询文档的最新版本。
     *
     * @param documentId 文档 ID
     * @return 版本号最大的版本；无版本时返回 {@code null}
     */
    DocumentVersion selectLatestVersion(@Param("documentId") String documentId);

    /**
     * 查询文档的最大版本号。
     *
     * @param documentId 文档 ID
     * @return 最大版本号；无版本时返回 0
     */
    Integer selectMaxVersionNumber(@Param("documentId") String documentId);

    /**
     * 查询文档的指定版本。
     *
     * @param documentId 文档 ID
     * @param versionNumber 版本号
     * @return 对应版本；不存在时返回 {@code null}
     */
    DocumentVersion selectByDocumentIdAndVersion(@Param("documentId") String documentId, @Param("versionNumber") Integer versionNumber);
}
