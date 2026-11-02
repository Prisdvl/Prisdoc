package com.javaee.documentservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置，声明文件上传与协同编辑的交换机、队列、绑定及 JSON 消息转换器。
 *
 * <p>队列与交换机均持久化，消息体统一使用 Jackson JSON 序列化，避免跨服务
 * 依赖 Java 原生序列化。</p>
 */
@Configuration
public class RabbitMQConfig {

    public static final String FILE_EXCHANGE = "file.exchange";
    public static final String FILE_UPLOAD_QUEUE = "file.upload.queue";
    public static final String FILE_UPLOAD_ROUTING_KEY = "file.upload";

    public static final String COLLABORATE_EXCHANGE = "collaborate.exchange";
    public static final String COLLABORATE_EDIT_QUEUE = "collaborate.edit.queue";
    public static final String COLLABORATE_EDIT_ROUTING_KEY = "collaborate.edit";

    @Bean
    public Queue fileUploadQueue() {
        return new Queue(FILE_UPLOAD_QUEUE, true);
    }

    @Bean
    public TopicExchange fileExchange() {
        return new TopicExchange(FILE_EXCHANGE, true, false);
    }

    @Bean
    public Binding fileUploadBinding() {
        return BindingBuilder
                .bind(fileUploadQueue())
                .to(fileExchange())
                .with(FILE_UPLOAD_ROUTING_KEY);
    }

    @Bean
    public Queue collaborateEditQueue() {
        return new Queue(COLLABORATE_EDIT_QUEUE, true);
    }

    @Bean
    public TopicExchange collaborateExchange() {
        return new TopicExchange(COLLABORATE_EXCHANGE, true, false);
    }

    @Bean
    public Binding collaborateEditBinding() {
        return BindingBuilder
                .bind(collaborateEditQueue())
                .to(collaborateExchange())
                .with(COLLABORATE_EDIT_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}