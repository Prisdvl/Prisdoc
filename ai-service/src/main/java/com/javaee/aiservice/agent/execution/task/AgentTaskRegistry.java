package com.javaee.aiservice.agent.execution.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * 任务运行注册表，保留最近一段时间的 Agent 任务执行快照。
 *
 * <p>支持按 traceId 查询、按 userId 列出与取消标记；内存中按 LRU 淘汰，
 * Redis 可用时同时持久化。</p>
 */
@Component
public class AgentTaskRegistry {

    private static final int MAX_TASKS = 200;
    private static final long TASK_TTL_DAYS = 7L;
    private static final String TASK_KEY_PREFIX = "agent:task:";
    private static final String TASK_ALL_ZSET = "agent:tasks:all";
    private static final String TASK_USER_ZSET_PREFIX = "agent:tasks:user:";

    private final ConcurrentMap<String, Map<String, Object>> tasks = new ConcurrentHashMap<>();
    private final java.util.Deque<String> evictionOrder = new java.util.concurrent.ConcurrentLinkedDeque<>();

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 保存任务快照，超过容量时淘汰最旧的记录。
     *
     * @param traceId 任务追踪 ID
     * @param snapshot 任务快照，为空时忽略
     */
    public void save(String traceId, Map<String, Object> snapshot) {
        if (traceId == null || snapshot == null) {
            return;
        }
        Map<String, Object> stored = new LinkedHashMap<>(snapshot);
        stored.putIfAbsent("traceId", traceId);
        stored.putIfAbsent("savedAt", System.currentTimeMillis());
        tasks.put(traceId, stored);
        evictionOrder.addLast(traceId);
        persist(traceId, stored);
        while (evictionOrder.size() > MAX_TASKS) {
            String oldest = evictionOrder.pollFirst();
            if (oldest != null) {
                tasks.remove(oldest);
            }
        }
    }

    /**
     * 查询任务快照，内存未命中时回源持久化存储。
     *
     * @param traceId 任务追踪 ID
     * @return 任务快照；不存在时返回空映射
     */
    public Map<String, Object> get(String traceId) {
        Map<String, Object> local = tasks.get(traceId);
        if (local != null) {
            return local;
        }
        Map<String, Object> persisted = load(traceId);
        if (!persisted.isEmpty()) {
            tasks.put(traceId, persisted);
            return persisted;
        }
        return Collections.emptyMap();
    }

    /**
     * 按审批令牌反查待确认的任务快照。
     *
     * @param token 审批令牌
     * @return 匹配的任务快照；无匹配时返回空映射
     */
    public Map<String, Object> findByApprovalToken(String token) {
        if (token == null || token.isBlank()) {
            return Collections.emptyMap();
        }
        for (Map<String, Object> snapshot : tasks.values()) {
            Object pending = snapshot.get("pendingApproval");
            if (pending instanceof Map<?, ?> map && token.equals(String.valueOf(map.get("agentApprovalToken")))) {
                return snapshot;
            }
        }
        for (Map<String, Object> snapshot : listAll()) {
            Object pending = snapshot.get("pendingApproval");
            if (pending instanceof Map<?, ?> map && token.equals(String.valueOf(map.get("agentApprovalToken")))) {
                Map<String, Object> loaded = new LinkedHashMap<>(snapshot);
                tasks.put(String.valueOf(loaded.get("traceId")), loaded);
                return loaded;
            }
        }
        return Collections.emptyMap();
    }

    /**
     * 列出指定用户的任务，按保存时间倒序排列。
     *
     * @param userId 用户 ID
     * @return 该用户的任务快照列表；用户为 null 时返回空列表
     */
    public List<Map<String, Object>> listByUser(String userId) {
        if (userId == null) {
            return List.of();
        }
        List<Map<String, Object>> persisted = listFromRedis(TASK_USER_ZSET_PREFIX + userId);
        if (!persisted.isEmpty()) {
            return persisted;
        }
        List<Map<String, Object>> matched = new ArrayList<>();
        for (Map<String, Object> snapshot : tasks.values()) {
            if (userId.equals(snapshot.get("userId"))) {
                matched.add(snapshot);
            }
        }
        matched.sort((a, b) -> Long.compare(asLong(b.get("savedAt")), asLong(a.get("savedAt"))));
        return matched;
    }

    /**
     * 将未结束的任务标记为已取消。
     *
     * @param traceId 任务追踪 ID
     * @return 是否成功取消：true-已取消，false-任务不存在或已结束
     */
    public boolean cancel(String traceId) {
        Map<String, Object> snapshot = tasks.get(traceId);
        if (snapshot == null) {
            return false;
        }
        Object status = snapshot.get("status");
        if ("success".equals(status) || "cancelled".equals(status) || "error".equals(status)) {
            return false;
        }
        snapshot.put("status", "cancelled");
        snapshot.put("cancelledAt", System.currentTimeMillis());
        persist(traceId, snapshot);
        return true;
    }

    /**
     * 判断任务是否已标记取消。
     *
     * @param traceId 任务追踪 ID
     * @return 是否已取消
     */
    public boolean isCancelled(String traceId) {
        Map<String, Object> snapshot = tasks.get(traceId);
        return snapshot != null && "cancelled".equals(snapshot.get("status"));
    }

    /**
     * 列出全部任务，按 savedAt 倒序排列，供管理员与调试使用。
     *
     * @return 全部任务快照列表
     */
    public List<Map<String, Object>> listAll() {
        List<Map<String, Object>> persisted = listFromRedis(TASK_ALL_ZSET);
        if (!persisted.isEmpty()) {
            return persisted;
        }
        List<Map<String, Object>> all = new ArrayList<>(tasks.values());
        all.sort((a, b) -> Long.compare(asLong(b.get("savedAt")), asLong(a.get("savedAt"))));
        return all;
    }

    /**
     * 删除指定任务的快照。
     *
     * @param traceId 任务追踪 ID
     * @return 是否删除成功：true-内存或持久化记录被移除，false-任务不存在
     */
    public boolean delete(String traceId) {
        Map<String, Object> removed = tasks.remove(traceId);
        boolean redisRemoved = deletePersisted(traceId);
        if (removed != null || redisRemoved) {
            evictionOrder.remove(traceId);
            return true;
        }
        return false;
    }

    private void persist(String traceId, Map<String, Object> snapshot) {
        if (redisTemplate == null) {
            return;
        }
        try {
            String key = TASK_KEY_PREFIX + traceId;
            long score = asLong(snapshot.get("savedAt"));
            redisTemplate.opsForValue().set(key, snapshot, TASK_TTL_DAYS, TimeUnit.DAYS);
            redisTemplate.opsForZSet().add(TASK_ALL_ZSET, traceId, score);
            Object userId = snapshot.get("userId");
            if (userId != null) {
                redisTemplate.opsForZSet().add(TASK_USER_ZSET_PREFIX + userId, traceId, score);
            }
        } catch (Exception ignored) {
            // Redis 不可用时仍保留内存快照，避免影响主链路。
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> load(String traceId) {
        if (redisTemplate == null || traceId == null) {
            return Collections.emptyMap();
        }
        try {
            Object stored = redisTemplate.opsForValue().get(TASK_KEY_PREFIX + traceId);
            if (stored instanceof Map<?, ?> map) {
                return new LinkedHashMap<>((Map<String, Object>) map);
            }
        } catch (Exception ignored) {
        }
        return Collections.emptyMap();
    }

    private List<Map<String, Object>> listFromRedis(String zsetKey) {
        if (redisTemplate == null) {
            return List.of();
        }
        try {
            Set<Object> ids = redisTemplate.opsForZSet().reverseRange(zsetKey, 0, MAX_TASKS - 1);
            if (ids == null || ids.isEmpty()) {
                return List.of();
            }
            List<Map<String, Object>> snapshots = new ArrayList<>();
            for (Object id : ids) {
                Map<String, Object> item = load(String.valueOf(id));
                if (!item.isEmpty()) {
                    snapshots.add(item);
                }
            }
            snapshots.sort((a, b) -> Long.compare(asLong(b.get("savedAt")), asLong(a.get("savedAt"))));
            return snapshots;
        } catch (Exception ignored) {
            return List.of();
        }
    }

    private boolean deletePersisted(String traceId) {
        if (redisTemplate == null || traceId == null) {
            return false;
        }
        try {
            Map<String, Object> snapshot = load(traceId);
            redisTemplate.delete(TASK_KEY_PREFIX + traceId);
            redisTemplate.opsForZSet().remove(TASK_ALL_ZSET, traceId);
            Object userId = snapshot.get("userId");
            if (userId != null) {
                redisTemplate.opsForZSet().remove(TASK_USER_ZSET_PREFIX + userId, traceId);
            }
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private long asLong(Object value) {
        if (value instanceof Number n) {
            return n.longValue();
        }
        try {
            return value == null ? 0L : Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
