package com.javaee.aiservice.agent;

/**
 * 知识索引任务状态机。
 */
public enum KnowledgeIndexStatus {
    /** 已创建，等待处理。 */
    PENDING,
    /** 正在解析文档。 */
    PARSING,
    /** 正在生成向量。 */
    EMBEDDING,
    /** 索引完成。 */
    INDEXED,
    /** 索引失败。 */
    FAILED
}
