package com.javaee.aiservice.async;

import lombok.Data;

import java.io.Serializable;

/**
 * 异步 AI 任务状态，持久化在 Redis 中供轮询查询。
 */
@Data
public class AsyncAIJobVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 任务 ID。 */
    private String jobId;
    /** 任务类型，区分对话、摘要、关键词等处理方式。 */
    private String type;
    /** 目标模型编码。 */
    private String model;
    /** 提交任务的用户 ID。 */
    private String userId;
    /** 任务状态：queued、running、success、failed。 */
    private String status;
    /** 提交时间戳，单位毫秒。 */
    private Long submittedAt;
    /** 开始执行时间戳，单位毫秒。 */
    private Long startedAt;
    /** 结束时间戳，单位毫秒。 */
    private Long finishedAt;
    /** 任务结果，结构随任务类型变化。 */
    private Object result;
    /** 失败原因，仅失败时有值。 */
    private String error;
}
