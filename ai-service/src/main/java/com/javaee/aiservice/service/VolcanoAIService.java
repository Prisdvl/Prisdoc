package com.javaee.aiservice.service;

import com.javaee.aiservice.agent.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * AI 调用服务，把提示词透传给 ChatService 并返回模型输出。
 *
 * <p>底层模型调用异常统一包装为运行时异常抛出，并保留原始异常信息。</p>
 */
@Service
public class VolcanoAIService {

    private static final Logger log = LoggerFactory.getLogger(VolcanoAIService.class);

    private final ChatService chatService;

    @Autowired
    public VolcanoAIService(ChatService chatService) {
        this.chatService = chatService;
    }

    public String callAI(String prompt) {
        log.info("使用阿里云百炼调用 AI API");
        log.info("Prompt: {}", prompt);

        try {
            String content = chatService.callChatApi(prompt);
            log.info("AI 返回: {}", content);
            return content;
        } catch (Exception e) {
            log.error("调用 AI API 失败", e);
            throw new RuntimeException("调用 AI API 失败: " + e.getMessage(), e);
        }
    }
}