package com.javaee.aiservice.agent.execution.model;

/**
 * Plan 步骤状态机，用枚举收敛状态字符串，避免散落的魔法值。
 */
public enum AgentStepStatus {
    /** 待处理。 */
    PENDING("pending"),
    /** 已规划。 */
    PLANNED("planned"),
    /** 执行中。 */
    RUNNING("running"),
    /** 执行成功。 */
    SUCCESS("success"),
    /** 执行失败。 */
    ERROR("error"),
    /** 因依赖不满足或条件跳过。 */
    SKIPPED("skipped"),
    /** 被阻塞，等待外部条件。 */
    BLOCKED("blocked"),
    /** 等待用户补充信息或确认。 */
    WAITING_USER("waiting_user"),
    /** 已取消。 */
    CANCELLED("cancelled");

    private final String value;

    AgentStepStatus(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    /**
     * 判断状态是否已终结。
     *
     * @param status 状态字符串
     * @return 是否为终结状态：true-已终结，false-仍在流转中
     */
    public static boolean isTerminal(String status) {
        return SUCCESS.value.equals(status)
                || ERROR.value.equals(status)
                || SKIPPED.value.equals(status)
                || BLOCKED.value.equals(status)
                || CANCELLED.value.equals(status);
    }

    /**
     * 判断状态是否为成功。
     *
     * @param status 状态字符串
     * @return 是否为 success
     */
    public static boolean isSuccess(String status) {
        return SUCCESS.value.equals(status);
    }
}
