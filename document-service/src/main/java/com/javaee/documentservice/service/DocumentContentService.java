package com.javaee.documentservice.service;

/**
 * 文档内容服务接口，负责文档正文在 MinIO 中的读写与删除。
 *
 * <p>每个操作都提供两个重载：按用户 ID 定位用户桶，或直接指定桶名以支持协作场景。</p>
 */
public interface DocumentContentService {

    /**
     * 把文档正文保存到该用户所在的对象存储桶。
     *
     * @param documentId 文档 ID
     * @param userId 文档所属用户 ID，用于定位存储桶
     * @param content 文档正文
     * @return 是否保存成功
     */
    boolean saveContent(String documentId, Long userId, String content);

    /**
     * 把文档正文保存到指定对象存储桶。
     *
     * @param documentId 文档 ID
     * @param bucketName 目标存储桶名
     * @param content 文档正文
     * @return 是否保存成功
     */
    boolean saveContent(String documentId, String bucketName, String content);

    /**
     * 从该用户所在的对象存储桶读取文档正文。
     *
     * @param documentId 文档 ID
     * @param userId 文档所属用户 ID，用于定位存储桶
     * @return 文档正文；对象不存在时返回 {@code null}
     */
    String getContent(String documentId, Long userId);

    /**
     * 从指定对象存储桶读取文档正文。
     *
     * @param documentId 文档 ID
     * @param bucketName 来源存储桶名
     * @return 文档正文；对象不存在时返回 {@code null}
     */
    String getContent(String documentId, String bucketName);

    /**
     * 删除该用户所在桶中的文档正文对象。
     *
     * @param documentId 文档 ID
     * @param userId 文档所属用户 ID，用于定位存储桶
     * @return 是否删除成功
     */
    boolean deleteContent(String documentId, Long userId);

    /**
     * 删除指定桶中的文档正文对象。
     *
     * @param documentId 文档 ID
     * @param bucketName 目标存储桶名
     * @return 是否删除成功
     */
    boolean deleteContent(String documentId, String bucketName);

    /**
     * 覆盖写入该用户所在桶中的文档正文。
     *
     * @param documentId 文档 ID
     * @param userId 文档所属用户 ID，用于定位存储桶
     * @param content 新的文档正文
     * @return 是否更新成功
     */
    boolean updateContent(String documentId, Long userId, String content);

    /**
     * 覆盖写入指定桶中的文档正文。
     *
     * @param documentId 文档 ID
     * @param bucketName 目标存储桶名
     * @param content 新的文档正文
     * @return 是否更新成功
     */
    boolean updateContent(String documentId, String bucketName, String content);

    /**
     * 获取用户对应的存储桶名。
     *
     * @param userId 用户 ID
     * @return 该用户的 MinIO 桶名
     */
    String getBucketName(Long userId);

    /**
     * 获取文档正文对应的对象名。
     *
     * @param documentId 文档 ID
     * @return MinIO 对象名
     */
    String getObjectName(String documentId);
}
