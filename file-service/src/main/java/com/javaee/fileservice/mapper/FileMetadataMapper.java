package com.javaee.fileservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.javaee.fileservice.entity.FileMetadata;

/**
 * 文件元数据数据访问接口，基于 MyBatis-Plus 操作 file_metadata 表。
 */
public interface FileMetadataMapper extends BaseMapper<FileMetadata> {

    /**
     * 根据文件 ID 查询文件元数据。
     *
     * @param fileId 文件 ID
     * @return 文件元数据；不存在时返回 null
     */
    FileMetadata selectByFileId(String fileId);

    /**
     * 按文件名关键词模糊搜索文件。
     *
     * @param keyword 文件名关键词
     * @return 匹配的元数据列表；无匹配时返回空列表
     */
    java.util.List<FileMetadata> searchByFileName(String keyword);

}
