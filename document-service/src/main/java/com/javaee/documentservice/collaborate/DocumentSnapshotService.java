package com.javaee.documentservice.collaborate;

import com.javaee.common.utils.UserBucketUtils;
import com.javaee.documentservice.entity.Document;
import com.javaee.documentservice.mapper.DocumentMapper;
import com.javaee.documentservice.service.DocumentContentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 文档协同快照服务，负责把协同内容落盘并按操作回放重建正文。
 *
 * <p>累计编辑操作达到 50 次时触发一次异步快照；快照写入失败只记录日志，
 * 不阻断协同流程，后续操作仍可基于上一次快照回放。</p>
 */
@Component
public class DocumentSnapshotService {

    private static final Logger log = LoggerFactory.getLogger(DocumentSnapshotService.class);

    private static final int SNAPSHOT_INTERVAL_OPS = 50;

    @Autowired
    private DocumentContentService documentContentService;

    @Autowired
    private DocumentMapper documentMapper;

    @Autowired
    private EditOperationHandler editOperationHandler;

    /**
     * 按累计操作数判断是否需要创建快照，达到阈值时异步落盘。
     *
     * @param documentId 文档 ID
     * @param currentContent 当前内存中的文档正文
     */
    @Async
    public void tryCreateSnapshot(String documentId, String currentContent) {
        List<EditOperation> recentOps = editOperationHandler.getRecentOperations(documentId);
        if (recentOps.size() >= SNAPSHOT_INTERVAL_OPS) {
            createSnapshot(documentId, currentContent);
        }
    }

    /**
     * 把当前正文写入对象存储作为新快照，失败仅记录日志。
     *
     * @param documentId 文档 ID
     * @param content 待保存的文档正文
     */
    public void createSnapshot(String documentId, String content) {
        try {
            documentContentService.updateContent(documentId, bucketNameFor(documentId), content);
            log.info("文档快照已创建: documentId={}, contentLength={}", documentId,
                    content != null ? content.length() : 0);
        } catch (Exception e) {
            log.error("创建文档快照失败: documentId={}", documentId, e);
        }
    }

    /**
     * 以最近一次快照为基线，回放缓存中的编辑操作重建当前正文。
     *
     * @param documentId 文档 ID
     * @return 重建后的文档正文；无快照时从空串开始回放
     */
    public String rebuildContent(String documentId) {
        String snapshotContent = documentContentService.getContent(documentId, bucketNameFor(documentId));
        if (snapshotContent == null) {
            snapshotContent = "";
        }

        List<EditOperation> operations = editOperationHandler.getRecentOperations(documentId);
        StringBuilder content = new StringBuilder(snapshotContent);

        for (EditOperation op : operations) {
            applyOperation(content, op);
        }

        return content.toString();
    }

    private String bucketNameFor(String documentId) {
        Document document = documentMapper.selectById(documentId);
        if (document == null) {
            throw new IllegalArgumentException("文档不存在: " + documentId);
        }
        if (document.getBucketName() != null && !document.getBucketName().isBlank()) {
            return document.getBucketName();
        }
        return UserBucketUtils.bucketNameForUser(document.getUserId());
    }

    private void applyOperation(StringBuilder content, EditOperation op) {
        if (op.getType() == EditOperation.OpType.INSERT) {
            if (op.getFrom() >= 0 && op.getFrom() <= content.length()) {
                content.insert(op.getFrom(), op.getText());
            }
        } else if (op.getType() == EditOperation.OpType.DELETE) {
            if (op.getFrom() >= 0 && op.getTo() <= content.length() && op.getFrom() < op.getTo()) {
                content.delete(op.getFrom(), op.getTo());
            }
        }
    }
}
