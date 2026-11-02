package com.javaee.documentservice.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javaee.documentservice.collaborate.CollaborateMessage;
import com.javaee.documentservice.collaborate.EditOperation;
import com.javaee.documentservice.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * 协同编辑消息监听器，接收其他实例投递的编辑操作并推送给本实例的 WebSocket 客户端。
 *
 * <p>反序列化或推送失败只记录日志，不重新入队，避免坏消息造成无限重试。</p>
 */
@Component
public class CollaborateMessageListener {

    private static final Logger log = LoggerFactory.getLogger(CollaborateMessageListener.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 消费协同编辑队列，把编辑操作广播到对应文档的主题。
     *
     * @param message 编辑操作的 JSON 字节数组
     */
    @RabbitListener(queues = RabbitMQConfig.COLLABORATE_EDIT_QUEUE)
    public void handleCollaborateEdit(byte[] message) {
        try {
            EditOperation operation = objectMapper.readValue(message, EditOperation.class);
            log.debug("收到跨实例编辑操作: opId={}, documentId={}, userId={}",
                    operation.getOpId(), operation.getDocumentId(), operation.getUserId());

            CollaborateMessage editMsg = CollaborateMessage.edit(operation);
            messagingTemplate.convertAndSend("/topic/doc/" + operation.getDocumentId(), editMsg);
        } catch (Exception e) {
            log.error("处理跨实例协同编辑消息失败", e);
        }
    }
}