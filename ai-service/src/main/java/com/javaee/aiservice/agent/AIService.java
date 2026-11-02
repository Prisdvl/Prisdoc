package com.javaee.aiservice.agent;

import com.javaee.aiservice.model.ModelType;

/**
 * AI 模型调用统一抽象，屏蔽不同厂商接口的调用差异。
 *
 * <p>实现类负责具体的协议适配与异常转换，上层通过 {@code AIServiceFactory} 获取实例。</p>
 */
public interface AIService {

    /**
     * 以单轮对话方式调用模型并返回文本结果。
     *
     * @param prompt 用户提示词
     * @return 模型返回的文本内容；无内容时返回空字符串
     */
    String callChat(String prompt);

    /**
     * 获取当前实现对应的模型类型。
     *
     * @return 模型类型枚举
     */
    ModelType getModelType();

    /**
     * 判断当前模型实例是否可用。
     *
     * @return 是否可用：true-可调用，false-已被禁用
     */
    boolean isAvailable();
}
