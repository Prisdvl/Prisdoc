package com.javaee.gateway.util;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ 消息发送工具，支持按交换机路由键或队列名投递。
 */
@Component
public class RabbitMQUtil {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 按交换机与路由键发送消息。
     *
     * @param exchange 交换机名称
     * @param routingKey 路由键
     * @param message 消息体，由 JSON 转换器序列化
     */
    public void send(String exchange, String routingKey, Object message) {
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
    }

    /**
     * 直接向指定队列发送消息。
     *
     * @param queueName 队列名称
     * @param message 消息体，由 JSON 转换器序列化
     */
    public void sendToQueue(String queueName, Object message) {
        rabbitTemplate.convertAndSend(queueName, message);
    }
}
