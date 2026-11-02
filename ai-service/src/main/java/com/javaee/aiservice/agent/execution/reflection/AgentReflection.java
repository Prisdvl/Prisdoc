package com.javaee.aiservice.agent.execution.reflection;

import com.javaee.aiservice.agent.execution.model.AgentPlanStep;

import java.util.ArrayList;
import java.util.List;

/**
 * 每轮 Agent 执行后产出的结构化反思结果。
 *
 * <p>督导模型判定是否已完成、是否继续执行、是否需要重规划，并给出质量分与建议动作；
 * 模型不可用时由启发式规则生成。</p>
 */
public class AgentReflection {

    /** 当前推理轮次。 */
    private int iteration;
    /** 任务是否已完成：true-已完成。 */
    private boolean complete;
    /** 是否继续执行后续轮次：true-继续。 */
    private boolean continueExecution;
    /** 是否需要重新规划：true-需要。 */
    private boolean requiresReplan;
    /** 是否需要向用户追问：true-需要。 */
    private boolean shouldAskUser;
    /** 质量评分，满分 100。 */
    private int qualityScore = 100;
    /** 结论置信度，取值 0 到 1。 */
    private double confidence = 1.0d;
    /** 反思结论说明。 */
    private String reason;
    /** 发现的问题列表。 */
    private List<String> issues = new ArrayList<>();
    /** 缺失的信息列表。 */
    private List<String> missingInfo = new ArrayList<>();
    /** 建议的后续动作列表。 */
    private List<String> recommendedActions = new ArrayList<>();
    /** 修订后的执行计划，可为空。 */
    private List<AgentPlanStep> revisedPlan = new ArrayList<>();
    /** 反思来源：model-模型生成，heuristic-启发式生成。 */
    private String source = "model";
    /** 是否因模型调用失败而降级：true-已降级。 */
    private boolean fallback;
    /** 反思生成时间戳，单位毫秒。 */
    private long createdAt = System.currentTimeMillis();

    public int getIteration() {
        return iteration;
    }

    public void setIteration(int iteration) {
        this.iteration = Math.max(0, iteration);
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public boolean isContinueExecution() {
        return continueExecution;
    }

    public void setContinueExecution(boolean continueExecution) {
        this.continueExecution = continueExecution;
    }

    public boolean isRequiresReplan() {
        return requiresReplan;
    }

    public void setRequiresReplan(boolean requiresReplan) {
        this.requiresReplan = requiresReplan;
    }

    public boolean isShouldAskUser() {
        return shouldAskUser;
    }

    public void setShouldAskUser(boolean shouldAskUser) {
        this.shouldAskUser = shouldAskUser;
    }

    public int getQualityScore() {
        return qualityScore;
    }

    public void setQualityScore(int qualityScore) {
        this.qualityScore = Math.max(0, Math.min(100, qualityScore));
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = Math.max(0.0d, Math.min(1.0d, confidence));
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public List<String> getIssues() {
        return issues;
    }

    public void setIssues(List<String> issues) {
        this.issues = issues != null ? issues : new ArrayList<>();
    }

    public List<String> getMissingInfo() {
        return missingInfo;
    }

    public void setMissingInfo(List<String> missingInfo) {
        this.missingInfo = missingInfo != null ? missingInfo : new ArrayList<>();
    }

    public List<String> getRecommendedActions() {
        return recommendedActions;
    }

    public void setRecommendedActions(List<String> recommendedActions) {
        this.recommendedActions = recommendedActions != null ? recommendedActions : new ArrayList<>();
    }

    public List<AgentPlanStep> getRevisedPlan() {
        return revisedPlan;
    }

    public void setRevisedPlan(List<AgentPlanStep> revisedPlan) {
        this.revisedPlan = revisedPlan != null ? revisedPlan : new ArrayList<>();
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public boolean isFallback() {
        return fallback;
    }

    public void setFallback(boolean fallback) {
        this.fallback = fallback;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
