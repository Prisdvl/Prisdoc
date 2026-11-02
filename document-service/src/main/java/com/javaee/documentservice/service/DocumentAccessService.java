package com.javaee.documentservice.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.javaee.common.exception.BusinessException;
import com.javaee.documentservice.entity.Document;
import com.javaee.documentservice.entity.DocumentAccess;
import com.javaee.documentservice.mapper.DocumentAccessMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Set;

/**
 * 文档协作权限服务，负责授权记录的写入与读写权限校验。
 *
 * <p>文档创建者视为所有者，无需授权记录即可读写；其余用户按 owner/editor/viewer
 * 角色判定，读权限三者皆有，写权限仅 owner 与 editor 拥有。</p>
 */
@Service
public class DocumentAccessService {

    private static final Set<String> READ_ROLES = Set.of("owner", "editor", "viewer");
    private static final Set<String> WRITE_ROLES = Set.of("owner", "editor");

    @Autowired
    private DocumentAccessMapper documentAccessMapper;

    /**
     * 为文档创建者写入 owner 授权记录。
     *
     * @param documentId 文档 ID
     * @param bucketName 文档所在存储桶
     * @param userId 文档创建者 ID
     */
    public void grantOwnerAccess(String documentId, String bucketName, Long userId) {
        grantAccess(documentId, bucketName, userId, "owner");
    }

    /**
     * 授予或更新用户对文档的协作角色。
     *
     * @param documentId 文档 ID
     * @param bucketName 文档所在存储桶
     * @param userId 被授权用户 ID
     * @param role 协作角色，为空时按 editor 处理
     */
    public void grantAccess(String documentId, String bucketName, Long userId, String role) {
        if (documentId == null || documentId.isBlank()) {
            throw new BusinessException("文档ID不能为空");
        }
        if (bucketName == null || bucketName.isBlank()) {
            throw new BusinessException("存储桶不能为空");
        }
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        String normalizedRole = normalizeRole(role);
        DocumentAccess existing = documentAccessMapper.selectOne(new QueryWrapper<DocumentAccess>()
                .eq("document_id", documentId)
                .eq("user_id", userId));
        LocalDateTime now = LocalDateTime.now();
        if (existing == null) {
            DocumentAccess access = new DocumentAccess();
            access.setDocumentId(documentId);
            access.setBucketName(bucketName);
            access.setUserId(userId);
            access.setRole(normalizedRole);
            access.setCreateTime(now);
            access.setUpdateTime(now);
            documentAccessMapper.insert(access);
            return;
        }
        existing.setBucketName(bucketName);
        existing.setRole(normalizedRole);
        existing.setUpdateTime(now);
        documentAccessMapper.updateById(existing);
    }

    /**
     * 校验用户具备文档的读权限，失败时抛出业务异常。
     *
     * @param document 目标文档
     * @param userId 当前用户 ID
     */
    public void assertCanRead(Document document, Long userId) {
        if (!canAccess(document, userId, READ_ROLES)) {
            throw new BusinessException("无权访问此文档");
        }
    }

    /**
     * 校验用户具备文档的写权限，失败时抛出业务异常。
     *
     * @param document 目标文档
     * @param userId 当前用户 ID
     */
    public void assertCanWrite(Document document, Long userId) {
        if (!canAccess(document, userId, WRITE_ROLES)) {
            throw new BusinessException("无权修改此文档");
        }
    }

    /**
     * 判断用户是否为文档创建者。
     *
     * @param document 目标文档
     * @param userId 当前用户 ID
     * @return 是文档创建者返回 {@code true}
     */
    public boolean isOwner(Document document, Long userId) {
        return document != null && userId != null
                && document.getUserId() != null && document.getUserId().equals(userId);
    }

    private boolean canAccess(Document document, Long userId, Set<String> allowedRoles) {
        if (document == null || userId == null) {
            return false;
        }
        if (isOwner(document, userId)) {
            return true;
        }
        Long count = documentAccessMapper.selectCount(new QueryWrapper<DocumentAccess>()
                .eq("document_id", document.getId())
                .eq("user_id", userId)
                .in("role", allowedRoles));
        return count != null && count > 0;
    }

    private String normalizeRole(String role) {
        String normalized = role == null ? "editor" : role.trim().toLowerCase(Locale.ROOT);
        if (!READ_ROLES.contains(normalized)) {
            throw new BusinessException("不支持的协作角色: " + role);
        }
        return normalized;
    }
}
