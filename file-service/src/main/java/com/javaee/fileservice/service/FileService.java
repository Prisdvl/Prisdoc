package com.javaee.fileservice.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件服务接口，定义上传、分片、下载、删除、重命名、移动与复制的统一契约。
 */
public interface FileService {

    /**
     * 上传单个文件并返回新生成的文件 ID。
     *
     * @param file 待上传的文件
     * @return 新生成的文件 ID
     */
    String upload(MultipartFile file);

    /**
     * 逐个上传多个文件。
     *
     * @param files 待上传的文件数组
     * @return 与入参顺序一致的文件 ID 数组
     */
    String[] uploadMultiple(MultipartFile[] files);

    /**
     * 保存大文件的一个分片，等待合并。
     *
     * @param chunk 当前分片文件
     * @param fileId 分片所属文件的唯一标识
     * @param chunkIndex 分片序号，从 0 开始
     * @param totalChunks 分片总数
     */
    void uploadChunk(MultipartFile chunk, String fileId, int chunkIndex, int totalChunks);

    /**
     * 按序合并已上传的分片并写入存储。
     *
     * @param fileId 分片所属文件的唯一标识
     * @param fileName 合并后的原始文件名，用于推断扩展名与内容类型
     * @return 合并后的文件 ID
     */
    String mergeChunk(String fileId, String fileName);

    /**
     * 读取文件内容。
     *
     * @param fileId 文件 ID
     * @return 文件的全部字节
     */
    byte[] download(String fileId);

    /**
     * 删除文件及其元数据。
     *
     * @param fileId 文件 ID
     */
    void delete(String fileId);

    /**
     * 重命名文件。
     *
     * @param fileId 文件 ID
     * @param newName 新的文件名，含扩展名
     */
    void rename(String fileId, String newName);

    /**
     * 移动文件到目标目录。
     *
     * @param fileId 文件 ID
     * @param targetPath 目标目录
     */
    void move(String fileId, String targetPath);

    /**
     * 复制文件到目标目录。
     *
     * @param fileId 源文件 ID
     * @param targetPath 目标目录
     * @return 复制生成的新文件 ID
     */
    String copy(String fileId, String targetPath);

}
