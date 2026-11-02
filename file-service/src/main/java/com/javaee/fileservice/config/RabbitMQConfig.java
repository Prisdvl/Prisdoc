package com.javaee.fileservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置类，声明文件事件的交换机、队列、绑定与 JSON 消息转换器。
 */
@Configuration
public class RabbitMQConfig {

    public static final String FILE_UPLOAD_QUEUE = "file.upload.queue";
    public static final String FILE_DOWNLOAD_QUEUE = "file.download.queue";
    public static final String FILE_PROCESS_QUEUE = "file.process.queue";
    public static final String FILE_DELETE_QUEUE = "file.delete.queue";

    public static final String FILE_EXCHANGE = "file.exchange";

    public static final String FILE_UPLOAD_ROUTING_KEY = "file.upload";
    public static final String FILE_DOWNLOAD_ROUTING_KEY = "file.download";
    public static final String FILE_PROCESS_ROUTING_KEY = "file.process";
    public static final String FILE_DELETE_ROUTING_KEY = "file.delete";

    /**
     * 声明文件事件使用的主题交换机。
     *
     * @return 持久化的主题交换机
     */
    @Bean
    public TopicExchange fileExchange() {
        return new TopicExchange(FILE_EXCHANGE, true, false);
    }

    /**
     * 声明文件上传事件队列。
     *
     * @return 持久化、非独占且不自动删除的队列
     */
    @Bean
    public Queue fileUploadQueue() {
        return new Queue(FILE_UPLOAD_QUEUE, true, false, false);
    }

    /**
     * 声明文件下载事件队列。
     *
     * @return 持久化、非独占且不自动删除的队列
     */
    @Bean
    public Queue fileDownloadQueue() {
        return new Queue(FILE_DOWNLOAD_QUEUE, true, false, false);
    }

    /**
     * 声明文件处理事件队列。
     *
     * @return 持久化、非独占且不自动删除的队列
     */
    @Bean
    public Queue fileProcessQueue() {
        return new Queue(FILE_PROCESS_QUEUE, true, false, false);
    }

    /**
     * 声明文件删除事件队列。
     *
     * @return 持久化、非独占且不自动删除的队列
     */
    @Bean
    public Queue fileDeleteQueue() {
        return new Queue(FILE_DELETE_QUEUE, true, false, false);
    }

    /**
     * 把上传队列绑定到文件交换机。
     *
     * @return 上传队列与交换机的绑定关系
     */
    @Bean
    public Binding bindingFileUploadQueue() {
        return BindingBuilder.bind(fileUploadQueue()).to(fileExchange()).with(FILE_UPLOAD_ROUTING_KEY);
    }

    /**
     * 把下载队列绑定到文件交换机。
     *
     * @return 下载队列与交换机的绑定关系
     */
    @Bean
    public Binding bindingFileDownloadQueue() {
        return BindingBuilder.bind(fileDownloadQueue()).to(fileExchange()).with(FILE_DOWNLOAD_ROUTING_KEY);
    }

    /**
     * 把处理队列绑定到文件交换机。
     *
     * @return 处理队列与交换机的绑定关系
     */
    @Bean
    public Binding bindingFileProcessQueue() {
        return BindingBuilder.bind(fileProcessQueue()).to(fileExchange()).with(FILE_PROCESS_ROUTING_KEY);
    }

    /**
     * 把删除队列绑定到文件交换机。
     *
     * @return 删除队列与交换机的绑定关系
     */
    @Bean
    public Binding bindingFileDeleteQueue() {
        return BindingBuilder.bind(fileDeleteQueue()).to(fileExchange()).with(FILE_DELETE_ROUTING_KEY);
    }

    /**
     * 创建 JSON 消息转换器，使消息体以 JSON 传输。
     *
     * @return Jackson2JsonMessageConverter 实例
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 创建装配了 JSON 消息转换器的 RabbitTemplate。
     *
     * @param connectionFactory Spring Boot 提供的 RabbitMQ 连接工厂
     * @return 用于发送消息的 RabbitTemplate
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}