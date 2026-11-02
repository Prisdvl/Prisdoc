package com.javaee.aiservice.async;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 投递到 RabbitMQ 的异步模型执行消息。
 */
@Data
public class AsyncAIJobMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 任务 ID。 */
    private String jobId;
    /** 任务类型，消费者据此选择处理逻辑。 */
    private String type;
    /** 目标模型编码。 */
    private String model;
    /** 提交任务的用户 ID。 */
    private String userId;
    /** 消息创建时间戳，单位毫秒。 */
    private Long createdAt;
    /** 任务载荷，键值形式存放请求参数。 */
    private Map<String, Object> payload = new HashMap<>();
}
