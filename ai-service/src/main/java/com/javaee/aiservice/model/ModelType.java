package com.javaee.aiservice.model;

/**
 * 可调用的 AI 模型清单，维护模型编码、展示名与供应商标识。
 *
 * <p>编码用于请求厂商接口，供应商用于选择对应的协议实现。</p>
 */
public enum ModelType {
    /** 通义千问 3.6 Plus，走 OpenAI 兼容协议。 */
    QWEN36_PLUS("qwen3.6-plus", "通义千问3.6 Plus", "openai"),
    /** 智谱 GLM-5。 */
    GLM5("glm-5", "智谱GLM-5", "openai"),
    /** 月之暗面 Kimi K2.5。 */
    KIMI_K25("kimi-k2.5", "月之暗面K2.5", "openai"),
    /** MiniMax M2.5 模型。 */
    MINIMAX_M25("MiniMax-M2.5", "MiniMax M2.5", "openai");

    /** 模型编码，用于调用厂商接口。 */
    private final String code;
    /** 模型展示名，用于日志与界面展示。 */
    private final String name;
    /** 供应商标识，用于选择协议实现。 */
    private final String provider;

    ModelType(String code, String name, String provider) {
        this.code = code;
        this.name = name;
        this.provider = provider;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getProvider() {
        return provider;
    }

    /**
     * 按模型编码查找模型类型，忽略大小写。
     *
     * @param code 模型编码
     * @return 匹配的模型类型；无匹配时返回默认模型 {@link #QWEN36_PLUS}
     */
    public static ModelType fromCode(String code) {
        for (ModelType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return QWEN36_PLUS;
    }
}
