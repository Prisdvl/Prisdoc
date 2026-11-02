package com.javaee.aiservice.async;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javaee.aiservice.config.AiRabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

/**
 * 异步 AI 任务服务，负责维护任务状态并把模型请求投递到 RabbitMQ。
 *
 * <p>任务状态存放在 Redis，过期时间由 {@code ai.async.job-expiry-hours} 配置；
 * 结果由消费者回写。</p>
 */
@Service
public class AsyncAIJobService {

    private static final String JOB_PREFIX = "ai:async:job:";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${ai.async.job-expiry-hours:24}")
    private long jobExpiryHours;

    /**
     * 创建异步任务并投递到消息队列。
     *
     * @param type 任务类型
     * @param payload 任务载荷，可为 null
     * @param model 目标模型编码
     * @param userId 提交任务的用户 ID
     * @return 新建任务的状态快照，状态为 queued
     */
    public AsyncAIJobVO submit(String type, Object payload, String model, String userId) {
        String jobId = UUID.randomUUID().toString();
        long now = System.currentTimeMillis();

        AsyncAIJobVO job = new AsyncAIJobVO();
        job.setJobId(jobId);
        job.setType(type);
        job.setModel(model);
        job.setUserId(userId);
        job.setStatus("queued");
        job.setSubmittedAt(now);
        save(job);

        AsyncAIJobMessage message = new AsyncAIJobMessage();
        message.setJobId(jobId);
        message.setType(type);
        message.setModel(model);
        message.setUserId(userId);
        message.setCreatedAt(now);
        message.setPayload(toMap(payload));

        rabbitTemplate.convertAndSend(
                AiRabbitMQConfig.AI_EXCHANGE,
                AiRabbitMQConfig.AI_MODEL_REQUEST_ROUTING_KEY,
                message
        );

        return job;
    }

    /**
     * 查询任务状态，非管理员只能查看自己的任务。
     *
     * @param jobId 任务 ID
     * @param userId 请求者用户 ID
     * @param admin 是否管理员：true-可跨用户查询
     * @return 任务状态快照
     */
    public AsyncAIJobVO getJob(String jobId, String userId, boolean admin) {
        AsyncAIJobVO job = load(jobId);
        if (job == null) {
            throw new IllegalArgumentException("异步任务不存在: " + jobId);
        }
        if (!admin && !userId.equals(job.getUserId())) {
            throw new SecurityException("无权访问该异步任务");
        }
        return job;
    }

    /**
     * 把任务标记为运行中并记录开始时间。
     *
     * @param jobId 任务 ID
     */
    public void markRunning(String jobId) {
        AsyncAIJobVO job = requireJob(jobId);
        job.setStatus("running");
        job.setStartedAt(System.currentTimeMillis());
        save(job);
    }

    /**
     * 把任务标记为成功并写入结果。
     *
     * @param jobId 任务 ID
     * @param result 任务结果
     */
    public void markSuccess(String jobId, Object result) {
        AsyncAIJobVO job = requireJob(jobId);
        job.setStatus("success");
        job.setResult(result);
        job.setFinishedAt(System.currentTimeMillis());
        save(job);
    }

    /**
     * 把任务标记为失败并记录错误信息。
     *
     * @param jobId 任务 ID
     * @param error 错误描述
     */
    public void markFailed(String jobId, String error) {
        AsyncAIJobVO job = requireJob(jobId);
        job.setStatus("failed");
        job.setError(error);
        job.setFinishedAt(System.currentTimeMillis());
        save(job);
    }

    private AsyncAIJobVO requireJob(String jobId) {
        AsyncAIJobVO job = load(jobId);
        if (job == null) {
            throw new IllegalArgumentException("异步任务不存在: " + jobId);
        }
        return job;
    }

    private AsyncAIJobVO load(String jobId) {
        Object value = redisTemplate.opsForValue().get(JOB_PREFIX + jobId);
        if (value instanceof AsyncAIJobVO job) {
            return job;
        }
        return objectMapper.convertValue(value, AsyncAIJobVO.class);
    }

    private void save(AsyncAIJobVO job) {
        redisTemplate.opsForValue().set(
                JOB_PREFIX + job.getJobId(),
                job,
                Duration.ofHours(jobExpiryHours)
        );
    }

    private Map<String, Object> toMap(Object payload) {
        if (payload == null) {
            return Map.of();
        }
        return objectMapper.convertValue(payload, new TypeReference<>() {});
    }
}
