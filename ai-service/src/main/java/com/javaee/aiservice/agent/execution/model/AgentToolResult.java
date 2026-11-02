package com.javaee.aiservice.agent.execution.model;

import java.util.HashMap;
import java.util.Map;

/**
 * 所有 Agent 工具统一返回的结果结构。
 */
public class AgentToolResult {

    /** 产生结果的工具名。 */
    private String toolName;
    /** 结果状态：success、error、action_required。 */
    private String status;
    /** 结果说明或错误信息。 */
    private String message;
    /** 是否需要用户补充操作：true-需要，false-不需要。 */
    private boolean requiresAction;
    /** 对应的步骤 ID。 */
    private String stepId;
    /** 开始时间戳，单位毫秒。 */
    private long startedAt;
    /** 结束时间戳，单位毫秒。 */
    private long finishedAt;
    /** 执行耗时，单位毫秒。 */
    private long durationMs;
    /** 结构化业务数据。 */
    private Map<String, Object> data = new HashMap<>();

    /**
     * 构造成功结果。
     *
     * @param toolName 工具名
     * @param message 结果说明
     * @param data 结构化数据
     * @return 状态为 success 的结果
     */
    public static AgentToolResult success(String toolName, String message, Map<String, Object> data) {
        AgentToolResult result = new AgentToolResult();
        result.setToolName(toolName);
        result.setStatus("success");
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    /**
     * 构造需要用户进一步操作的结果。
     *
     * @param toolName 工具名
     * @param message 提示用户的信息
     * @param data 结构化数据，可携带审批令牌
     * @return 状态为 action_required 的结果
     */
    public static AgentToolResult actionRequired(String toolName, String message, Map<String, Object> data) {
        AgentToolResult result = success(toolName, message, data);
        result.setStatus("action_required");
        result.setRequiresAction(true);
        return result;
    }

    /**
     * 构造失败结果。
     *
     * @param toolName 工具名
     * @param message 错误信息
     * @return 状态为 error 的结果
     */
    public static AgentToolResult error(String toolName, String message) {
        AgentToolResult result = new AgentToolResult();
        result.setToolName(toolName);
        result.setStatus("error");
        result.setMessage(message);
        return result;
    }

    public String getToolName() {
        return toolName;
    }

    public void setToolName(String toolName) {
        this.toolName = toolName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isRequiresAction() {
        return requiresAction;
    }

    public void setRequiresAction(boolean requiresAction) {
        this.requiresAction = requiresAction;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data != null ? data : new HashMap<>();
    }

    public String getStepId() {
        return stepId;
    }

    public void setStepId(String stepId) {
        this.stepId = stepId;
    }

    public long getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(long startedAt) {
        this.startedAt = startedAt;
    }

    public long getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(long finishedAt) {
        this.finishedAt = finishedAt;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }
}
