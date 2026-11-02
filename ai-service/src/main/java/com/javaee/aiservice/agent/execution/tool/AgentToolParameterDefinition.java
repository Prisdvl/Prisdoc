package com.javaee.aiservice.agent.execution.tool;

import java.util.List;

/**
 * 单个 Agent 工具参数的模式定义，供校验与前端渲染使用。
 */
public class AgentToolParameterDefinition {

    /** 参数名。 */
    private final String name;
    /** 参数类型：string、integer、boolean。 */
    private final String type;
    /** 参数含义说明。 */
    private final String description;
    /** 是否必填：true-必填，false-可选。 */
    private final boolean required;
    /** 默认值，无默认时为 null。 */
    private final Object defaultValue;
    /** 允许的取值集合，null 表示不限制。 */
    private final List<Object> allowedValues;
    /** 数值下界，null 表示不限制。 */
    private final Number minValue;
    /** 数值上界，null 表示不限制。 */
    private final Number maxValue;
    /** 字符串匹配模式，null 表示不校验。 */
    private final String pattern;

    public AgentToolParameterDefinition(String name, String type, String description, boolean required, Object defaultValue) {
        this(name, type, description, required, defaultValue, null, null, null, null);
    }

    public AgentToolParameterDefinition(String name, String type, String description, boolean required,
                                        Object defaultValue, List<Object> allowedValues,
                                        Number minValue, Number maxValue, String pattern) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.required = required;
        this.defaultValue = defaultValue;
        this.allowedValues = allowedValues;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.pattern = pattern;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public boolean isRequired() {
        return required;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public List<Object> getAllowedValues() {
        return allowedValues;
    }

    public Number getMinValue() {
        return minValue;
    }

    public Number getMaxValue() {
        return maxValue;
    }

    public String getPattern() {
        return pattern;
    }
}
