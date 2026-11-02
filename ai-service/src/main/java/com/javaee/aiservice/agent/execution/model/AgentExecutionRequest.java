package com.javaee.aiservice.agent.execution.model;

import java.util.HashMap;
import java.util.Map;

/**
 * 产品级 Agent 执行链的统一请求参数。
 */
public class AgentExecutionRequest {

    /** 用户原始任务描述。 */
    private String task;
    /** 会话 ID，为空表示新建会话。 */
    private String conversationId;
    /** 请求用户 ID。 */
    private String userId = "default";
    /** 指定模型编码，为空时使用默认模型。 */
    private String model;
    /** 是否启用 RAG 检索增强：true-启用，false-关闭。 */
    private Boolean ragEnabled = true;
    /** 是否只做规划不真正执行工具：true-预演，false-实际执行。 */
    private Boolean dryRun = false;
    /** 最大推理轮次。 */
    private Integer maxIterations = 3;
    /** 单次执行允许的最大工具调用次数。 */
    private Integer maxToolCalls = 8;
    /** 检索使用的知识库 ID。 */
    private String knowledgeBaseId = "default";
    /** 失败后是否自动重新规划：true-允许，false-禁止。 */
    private Boolean autoReplan = true;
    /** 是否启用反思环节，null 表示取服务端默认配置。 */
    private Boolean reflectionEnabled;
    /** 是否返回中间步骤明细：true-返回，false-只返回最终答案。 */
    private Boolean returnIntermediateSteps = true;
    /** 附加上下文变量，供模板引用。 */
    private Map<String, Object> context = new HashMap<>();
    /** 继续执行的追踪 ID，用于恢复历史任务。 */
    private String continueTraceId;
    /** 危险操作的用户审批令牌。 */
    private String approvalToken;

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Boolean getRagEnabled() {
        return ragEnabled;
    }

    public void setRagEnabled(Boolean ragEnabled) {
        this.ragEnabled = ragEnabled;
    }

    public Boolean getDryRun() {
        return dryRun;
    }

    public void setDryRun(Boolean dryRun) {
        this.dryRun = dryRun;
    }

    public Integer getMaxIterations() {
        return maxIterations;
    }

    public void setMaxIterations(Integer maxIterations) {
        this.maxIterations = maxIterations;
    }

    public Integer getMaxToolCalls() {
        return maxToolCalls;
    }

    public void setMaxToolCalls(Integer maxToolCalls) {
        this.maxToolCalls = maxToolCalls;
    }

    public String getKnowledgeBaseId() {
        return knowledgeBaseId;
    }

    public void setKnowledgeBaseId(String knowledgeBaseId) {
        this.knowledgeBaseId = knowledgeBaseId;
    }

    public Boolean getAutoReplan() {
        return autoReplan;
    }

    public void setAutoReplan(Boolean autoReplan) {
        this.autoReplan = autoReplan;
    }

    public Boolean getReflectionEnabled() {
        return reflectionEnabled;
    }

    public void setReflectionEnabled(Boolean reflectionEnabled) {
        this.reflectionEnabled = reflectionEnabled;
    }

    public Boolean getReturnIntermediateSteps() {
        return returnIntermediateSteps;
    }

    public void setReturnIntermediateSteps(Boolean returnIntermediateSteps) {
        this.returnIntermediateSteps = returnIntermediateSteps;
    }

    public Map<String, Object> getContext() {
        return context;
    }

    public void setContext(Map<String, Object> context) {
        this.context = context != null ? context : new HashMap<>();
    }

    public String getContinueTraceId() {
        return continueTraceId;
    }

    public void setContinueTraceId(String continueTraceId) {
        this.continueTraceId = continueTraceId;
    }

    public String getApprovalToken() {
        return approvalToken;
    }

    public void setApprovalToken(String approvalToken) {
        this.approvalToken = approvalToken;
    }
}
