package com.javaee.fileservice.util;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ 工具类，封装交换机与队列的消息发送。
 */
@Component
public class RabbitMQUtil {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 通过交换机与路由键发送消息。
     *
     * @param exchange 交换机名称
     * @param routingKey 路由键
     * @param message 消息体，由 JSON 消息转换器序列化
     */
    public void send(String exchange, String routingKey, Object message) {
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
    }

    /**
     * 直接向指定队列发送消息，走默认交换机。
     *
     * @param queueName 队列名称
     * @param message 消息体，由 JSON 消息转换器序列化
     */
    public void sendToQueue(String queueName, Object message) {
        rabbitTemplate.convertAndSend(queueName, message);
    }
}
