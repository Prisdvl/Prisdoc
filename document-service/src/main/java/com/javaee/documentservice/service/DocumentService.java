package com.javaee.documentservice.service;

import com.javaee.documentservice.dto.DocumentCreateDTO;
import com.javaee.documentservice.dto.DocumentQueryDTO;
import com.javaee.documentservice.dto.DocumentUpdateDTO;
import com.javaee.documentservice.entity.DocumentVersion;
import com.javaee.documentservice.vo.DocumentVO;

import java.util.List;

/**
 * 文档服务接口，定义文档的增删改查、协作授权与版本控制能力。
 *
 * <p>除创建外，所有操作都要求传入当前用户 ID，用于访问权限校验与操作留痕。</p>
 */
public interface DocumentService {

    /**
     * 创建文档并保存初始版本。
     *
     * @param dto 创建文档请求参数
     * @param userId 创建用户 ID
     * @return 新建文档信息
     */
    DocumentVO create(DocumentCreateDTO dto, Long userId);

    /**
     * 更新文档内容，并将更新前的正文保存为历史版本。
     *
     * @param id 文档 ID
     * @param dto 更新文档请求参数
     * @param userId 更新用户 ID
     * @return 更新后的文档信息
     */
    DocumentVO update(String id, DocumentUpdateDTO dto, Long userId);

    /**
     * 软删除文档，仅把状态标记为已删除。
     *
     * @param id 文档 ID
     * @param userId 执行删除的用户 ID
     */
    void delete(String id, Long userId);

    /**
     * 授予协作者访问文档的角色。
     *
     * @param id 文档 ID
     * @param collaboratorUserId 协作者用户 ID
     * @param role 协作角色，支持 owner/editor/viewer
     * @param operatorUserId 执行授权的用户 ID，需为文档所有者
     */
    void grantAccess(String id, Long collaboratorUserId, String role, Long operatorUserId);

    /**
     * 按 ID 获取文档详情。
     *
     * @param id 文档 ID
     * @param userId 当前用户 ID，用于权限校验
     * @return 文档详情
     */
    DocumentVO getById(String id, Long userId);

    /**
     * 获取文档在 MinIO 中的存储位置。
     *
     * @param id 文档 ID
     * @param userId 当前用户 ID，用于权限校验
     * @return 文档信息，含桶名与对象名
     */
    DocumentVO getStorageLocation(String id, Long userId);

    /**
     * 获取用户的文档列表。
     *
     * @param userId 用户 ID
     * @return 该用户的活跃文档列表
     */
    List<DocumentVO> getByUserId(Long userId);

    /**
     * 搜索文档。
     *
     * @param dto 查询参数，支持关键词与分类
     * @param userId 当前用户 ID，用于限定可访问范围
     * @return 命中的文档列表
     */
    List<DocumentVO> search(DocumentQueryDTO dto, Long userId);

    /**
     * 获取文档的所有历史版本。
     *
     * @param documentId 文档 ID
     * @param userId 当前用户 ID，用于权限校验
     * @return 历史版本列表，按版本号降序排列
     */
    List<DocumentVersion> getVersions(String documentId, Long userId);

    /**
     * 把文档恢复到指定版本。
     *
     * @param documentId 文档 ID
     * @param versionNumber 目标版本号
     * @param userId 操作用户 ID
     * @return 恢复后的文档信息
     */
    DocumentVO restoreVersion(String documentId, Integer versionNumber, Long userId);
}
