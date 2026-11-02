package com.javaee.fileservice.service;

import com.javaee.fileservice.entity.FileMetadata;
import java.util.List;

/**
 * 文件元数据服务接口，定义元数据的增删改查、分页搜索与目录结构查询。
 */
public interface FileMetadataService {

    /**
     * 根据文件 ID 查询元数据。
     *
     * @param fileId 文件 ID
     * @return 文件元数据；不存在或不属于当前存储桶时返回 null
     */
    FileMetadata getMetadata(String fileId);

    /**
     * 保存文件元数据，并补全时间与状态字段。
     *
     * @param fileMetadata 待保存的元数据
     */
    void saveMetadata(FileMetadata fileMetadata);

    /**
     * 按文件 ID 更新元数据。
     *
     * @param fileMetadata 待更新的元数据，按其中的文件 ID 定位记录
     */
    void updateMetadata(FileMetadata fileMetadata);

    /**
     * 删除指定文件的元数据。
     *
     * @param fileId 文件 ID
     */
    void deleteMetadata(String fileId);

    /**
     * 分页查询文件列表。
     *
     * @param page 页码，从 1 开始
     * @param size 每页条数
     * @param sortBy 排序字段，驼峰命名会被转换为数据库列名
     * @param direction 排序方向，asc 为升序，其余取值按降序
     * @return 当前页的元数据列表
     */
    List<FileMetadata> getFileList(int page, int size, String sortBy, String direction);

    /**
     * 按关键词分页搜索文件。
     *
     * @param keyword 搜索关键词，匹配文件名与原始文件名
     * @param page 页码，从 1 开始
     * @param size 每页条数
     * @return 匹配的元数据列表
     */
    List<FileMetadata> searchFiles(String keyword, int page, int size);

    /**
     * 查询指定路径的目录结构。
     *
     * @param path 目录路径
     * @return 目录结构，包含子目录、文件与当前路径
     */
    Object getDirectoryStructure(String path);

} 
