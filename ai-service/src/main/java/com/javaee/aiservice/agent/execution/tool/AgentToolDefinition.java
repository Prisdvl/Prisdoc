package com.javaee.aiservice.agent.execution.tool;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 暴露给规划器的工具元数据，保证工具选择口径一致。
 */
public class AgentToolDefinition {

    /** 工具名。 */
    private final String name;
    /** 工具用途说明。 */
    private final String description;
    /** 参数名到说明的兼容映射。 */
    private final Map<String, String> parameters;
    /** 参数名到参数模式的映射。 */
    private final Map<String, AgentToolParameterDefinition> parameterSchema;
    /** 是否为危险操作：true-危险，需审批。 */
    private final boolean destructive;
    /** 工具分类，如 rag、file、text。 */
    private final String category;
    /** 是否需要用户补充操作：true-需要。 */
    private final boolean requiresUserAction;
    /** 风险等级：low、high。 */
    private final String riskLevel;

    public AgentToolDefinition(String name, String description, Map<String, String> parameters, boolean destructive) {
        this(name, description, parameters, destructive, "general", false);
    }

    public AgentToolDefinition(String name, String description, Map<String, String> parameters,
                               boolean destructive, String category, boolean requiresUserAction) {
        this(name, description, parameters, toLegacySchema(parameters), destructive, category,
                requiresUserAction, destructive ? "high" : "low");
    }

    public AgentToolDefinition(String name, String description, Map<String, String> parameters,
                               Map<String, AgentToolParameterDefinition> parameterSchema,
                               boolean destructive, String category, boolean requiresUserAction, String riskLevel) {
        this.name = name;
        this.description = description;
        this.parameters = parameters != null ? parameters : new LinkedHashMap<>();
        this.parameterSchema = parameterSchema != null ? parameterSchema : new LinkedHashMap<>();
        this.destructive = destructive;
        this.category = category;
        this.requiresUserAction = requiresUserAction;
        this.riskLevel = riskLevel;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public Map<String, AgentToolParameterDefinition> getParameterSchema() {
        return parameterSchema;
    }

    public boolean isDestructive() {
        return destructive;
    }

    public String getCategory() {
        return category;
    }

    public boolean isRequiresUserAction() {
        return requiresUserAction;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    private static Map<String, AgentToolParameterDefinition> toLegacySchema(Map<String, String> parameters) {
        Map<String, AgentToolParameterDefinition> schema = new LinkedHashMap<>();
        if (parameters == null) {
            return schema;
        }
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            schema.put(entry.getKey(), new AgentToolParameterDefinition(
                    entry.getKey(), "string", entry.getValue(), false, null));
        }
        return schema;
    }
}
