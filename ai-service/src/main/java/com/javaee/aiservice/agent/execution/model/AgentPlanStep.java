package com.javaee.aiservice.agent.execution.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Agent 规划器生成的一个可执行步骤。
 */
public class AgentPlanStep {

    /** 步骤 ID，同一次执行内唯一。 */
    private String id;
    /** 步骤描述，用于展示与日志。 */
    private String description;
    /** 该步骤调用的工具名。 */
    private String toolName;
    /** 工具入参，可包含模板变量。 */
    private Map<String, Object> params = new HashMap<>();
    /** 步骤状态，取值见 {@link AgentStepStatus}。 */
    private String status = "pending";
    /** 工具返回的观察结果。 */
    private String observation;
    /** 规划时给出的推理说明。 */
    private String reasoning;
    /** 执行阶段生成的思考摘要。 */
    private String thought;
    /** 依赖的前序步骤 ID 列表。 */
    private List<String> dependsOn = new ArrayList<>();
    /** 成功判定条件，语法见执行服务的断言解析。 */
    private String successCriteria;
    /** 重试策略：none-不重试，其余取值对应退避策略。 */
    private String retryPolicy = "none";
    /** 最大重试次数。 */
    private int maxRetries;
    /** 已尝试次数。 */
    private int attempts;
    /** 风险等级：low、medium、high。 */
    private String riskLevel = "low";
    /** 是否需要用户审批：true-需审批，false-可直接执行。 */
    private boolean requiresApproval;
    /** 最近一次失败原因。 */
    private String lastError;

    public AgentPlanStep() {
    }

    public AgentPlanStep(String id, String description, String toolName, Map<String, Object> params) {
        this.id = id;
        this.description = description;
        this.toolName = toolName;
        this.params = params != null ? params : new HashMap<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getToolName() {
        return toolName;
    }

    public void setToolName(String toolName) {
        this.toolName = toolName;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params != null ? params : new HashMap<>();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    public String getReasoning() {
        return reasoning;
    }

    public void setReasoning(String reasoning) {
        this.reasoning = reasoning;
    }

    public String getThought() {
        return thought;
    }

    public void setThought(String thought) {
        this.thought = thought;
    }

    public List<String> getDependsOn() {
        return dependsOn;
    }

    public void setDependsOn(List<String> dependsOn) {
        this.dependsOn = dependsOn != null ? dependsOn : new ArrayList<>();
    }

    public String getSuccessCriteria() {
        return successCriteria;
    }

    public void setSuccessCriteria(String successCriteria) {
        this.successCriteria = successCriteria;
    }

    public String getRetryPolicy() {
        return retryPolicy;
    }

    public void setRetryPolicy(String retryPolicy) {
        this.retryPolicy = retryPolicy;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = Math.max(0, maxRetries);
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = Math.max(0, attempts);
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public boolean isRequiresApproval() {
        return requiresApproval;
    }

    public void setRequiresApproval(boolean requiresApproval) {
        this.requiresApproval = requiresApproval;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }
}
