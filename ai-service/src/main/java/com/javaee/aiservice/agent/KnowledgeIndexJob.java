package com.javaee.aiservice.agent;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 知识索引任务的状态记录，持有进度、重试次数与质量评估结果。
 */
public class KnowledgeIndexJob {

    private final String jobId;
    private final String documentId;
    private final String userId;
    private final String knowledgeBaseId;
    private final long createdAt;
    /** 任务状态，默认待处理。 */
    private volatile KnowledgeIndexStatus status = KnowledgeIndexStatus.PENDING;
    /** 最近更新时间戳，单位毫秒。 */
    private volatile long updatedAt;
    /** 已尝试执行次数。 */
    private volatile int attempts;
    /** 失败原因，成功时为 null。 */
    private volatile String errorMessage;
    /** 索引质量评估结果。 */
    private volatile Map<String, Object> quality = new LinkedHashMap<>();
    /** 处理进度，取值 0 到 100。 */
    private volatile int progress;
    /** 文档内容摘要，用于重复检测。 */
    private volatile String contentHash;
    /** 是否复用已有索引：true-内容未变更，直接复用。 */
    private volatile boolean reused;

    public KnowledgeIndexJob(String jobId, String documentId, String userId, String knowledgeBaseId) {
        this.jobId = jobId;
        this.documentId = documentId;
        this.userId = userId;
        this.knowledgeBaseId = knowledgeBaseId;
        this.createdAt = Instant.now().toEpochMilli();
        this.updatedAt = this.createdAt;
    }

    public String getJobId() {
        return jobId;
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getUserId() {
        return userId;
    }

    public String getKnowledgeBaseId() {
        return knowledgeBaseId;
    }

    public KnowledgeIndexStatus getStatus() {
        return status;
    }

    public void setStatus(KnowledgeIndexStatus status) {
        this.status = status;
        this.updatedAt = Instant.now().toEpochMilli();
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public int getAttempts() {
        return attempts;
    }

    public void incrementAttempts() {
        this.attempts++;
    }

    public void setAttempts(int attempts) {
        this.attempts = Math.max(0, attempts);
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Map<String, Object> getQuality() {
        return quality;
    }

    public void setQuality(Map<String, Object> quality) {
        this.quality = quality != null ? quality : new LinkedHashMap<>();
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = Math.max(0, Math.min(100, progress));
        this.updatedAt = Instant.now().toEpochMilli();
    }

    public String getContentHash() {
        return contentHash;
    }

    public void setContentHash(String contentHash) {
        this.contentHash = contentHash;
    }

    public boolean isReused() {
        return reused;
    }

    public void setReused(boolean reused) {
        this.reused = reused;
    }

    public Map<String, Object> snapshot() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("jobId", jobId);
        map.put("documentId", documentId);
        map.put("userId", userId);
        map.put("knowledgeBaseId", knowledgeBaseId);
        map.put("status", status.name());
        map.put("attempts", attempts);
        map.put("progress", progress);
        map.put("contentHash", contentHash);
        map.put("reused", reused);
        map.put("createdAt", createdAt);
        map.put("updatedAt", updatedAt);
        map.put("errorMessage", errorMessage);
        map.put("quality", quality);
        return map;
    }
}
