package com.javaee.aiservice.agent.execution.approval;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;

/**
 * 危险 Agent 工具的服务端审批存储，用一次性令牌校验用户确认结果。
 *
 * <p>令牌与「用户 + 工具 + 关键参数」的指纹绑定，默认有效期由
 * {@code ai.agent.approval-expiry-seconds} 配置，校验通过后立即失效。</p>
 */
@Service
public class AgentApprovalService {

    private static final String PREFIX = "agent:approval:";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${ai.agent.approval-expiry-seconds:300}")
    private long approvalExpirySeconds;

    /**
     * 创建一次性审批令牌。
     *
     * @param userId 发起操作的用户 ID
     * @param toolName 待执行的工具名
     * @param params 工具参数，参与指纹计算
     * @return 审批挑战，含令牌与过期时间戳
     */
    public ApprovalChallenge createChallenge(String userId, String toolName, Map<String, Object> params) {
        String token = UUID.randomUUID().toString();
        String fingerprint = fingerprint(userId, toolName, params);
        redisTemplate.opsForValue().set(PREFIX + token, fingerprint, Duration.ofSeconds(approvalExpirySeconds));
        return new ApprovalChallenge(token, System.currentTimeMillis() + approvalExpirySeconds * 1000);
    }

    /**
     * 校验审批令牌并消费，返回具体失败原因。
     *
     * @param token 审批令牌
     * @param userId 发起操作的用户 ID
     * @param toolName 待执行的工具名
     * @param params 工具参数，需与签发时一致
     * @return 校验结果：OK 表示通过，其余为缺失、过期或参数不匹配
     */
    public ApprovalResult verifyAndConsumeDetailed(String token, String userId, String toolName, Map<String, Object> params) {
        if (token == null || token.isBlank()) {
            return ApprovalResult.MISSING;
        }
        String key = PREFIX + token;
        Object expected = redisTemplate.opsForValue().get(key);
        if (expected == null) {
            return ApprovalResult.EXPIRED_OR_USED;
        }
        String actual = fingerprint(userId, toolName, params);
        if (!expected.toString().equals(actual)) {
            return ApprovalResult.PARAMS_MISMATCH;
        }
        redisTemplate.delete(key);
        return ApprovalResult.OK;
    }

    /**
     * 校验审批令牌并消费，仅返回是否通过。
     *
     * @param token 审批令牌
     * @param userId 发起操作的用户 ID
     * @param toolName 待执行的工具名
     * @param params 工具参数，需与签发时一致
     * @return 是否通过校验
     */
    public boolean verifyAndConsume(String token, String userId, String toolName, Map<String, Object> params) {
        return verifyAndConsumeDetailed(token, userId, toolName, params) == ApprovalResult.OK;
    }

    /**
     * 撤销尚未使用的审批令牌。
     *
     * @param token 审批令牌
     * @return 是否撤销成功：true-令牌存在并已删除，false-令牌不存在
     */
    public boolean cancel(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        return Boolean.TRUE.equals(redisTemplate.delete(PREFIX + token));
    }

    private String fingerprint(String userId, String toolName, Map<String, Object> params) {
        String objectName = String.valueOf(params.getOrDefault("objectName", ""));
        String targetVersionId = String.valueOf(params.getOrDefault("targetVersionId", ""));
        String recycleId = String.valueOf(params.getOrDefault("recycleId", ""));
        String raw = userId + "|" + toolName + "|" + objectName + "|" + targetVersionId + "|" + recycleId;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("生成审批指纹失败", e);
        }
    }

    /**
     * 审批挑战数据，返回给前端提示用户确认。
     *
     * @param token 一次性审批令牌
     * @param expiresAt 令牌过期时间戳，单位毫秒
     */
    public record ApprovalChallenge(String token, long expiresAt) {
    }

    /**
     * 审批校验结果。
     */
    public enum ApprovalResult {
        /** 校验通过，令牌已消费。 */
        OK,
        /** 未提供令牌。 */
        MISSING,
        /** 令牌不存在，已过期或已被使用。 */
        EXPIRED_OR_USED,
        /** 参数与签发时不一致。 */
        PARAMS_MISMATCH
    }
}
