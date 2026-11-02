package com.javaee.aiservice.conversation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 对话上下文管理器，基于 Redis 保存、读取、合并与清理对话上下文。
 *
 * <p>上下文以 Redis Hash 存储并按对话 ID 隔离，读写异常直接向上抛出，由调用方处理。</p>
 */
@Component
public class ContextManager {

    private static final Logger log = LoggerFactory.getLogger(ContextManager.class);
    private static final String CONTEXT_PREFIX = "ctx:";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 获取指定对话的全部上下文。
     *
     * @param conversationId 对话 ID
     * @return 上下文键值对，不存在时返回空 Map
     */
    public Map<String, Object> getContext(String conversationId) {
        String key = CONTEXT_PREFIX + conversationId;
        
        Map<Object, Object> hash = redisTemplate.opsForHash().entries(key);
        Map<String, Object> context = new HashMap<>();
        for (Map.Entry<Object, Object> entry : hash.entrySet()) {
            context.put(entry.getKey().toString(), entry.getValue());
        }
        
        log.debug("获取上下文: conversationId={}, size={}", conversationId, context.size());
        return context;
    }

    /**
     * 批量更新对话上下文，更新内容为空时直接返回。
     *
     * @param conversationId 对话 ID
     * @param updates 待写入的上下文字段
     */
    public void updateContext(String conversationId, Map<String, Object> updates) {
        if (updates == null || updates.isEmpty()) {
            return;
        }

        String key = CONTEXT_PREFIX + conversationId;
        redisTemplate.opsForHash().putAll(key, updates);
        
        log.debug("更新上下文: conversationId={}, updates={}", conversationId, updates.size());
    }

    /**
     * 设置单个上下文字段。
     *
     * @param conversationId 对话 ID
     * @param key 字段名
     * @param value 字段值
     */
    public void setContextValue(String conversationId, String key, Object value) {
        String contextKey = CONTEXT_PREFIX + conversationId;
        redisTemplate.opsForHash().put(contextKey, key, value);
        
        log.debug("设置上下文字段: conversationId={}, key={}", conversationId, key);
    }

    /**
     * 获取单个上下文字段的值。
     *
     * @param conversationId 对话 ID
     * @param key 字段名
     * @return 字段值，不存在时返回 null
     */
    public Object getContextValue(String conversationId, String key) {
        String contextKey = CONTEXT_PREFIX + conversationId;
        return redisTemplate.opsForHash().get(contextKey, key);
    }

    /**
     * 清理指定对话的全部上下文。
     *
     * @param conversationId 对话 ID
     */
    public void clearContext(String conversationId) {
        String key = CONTEXT_PREFIX + conversationId;
        redisTemplate.delete(key);
        
        log.debug("清理上下文: conversationId={}", conversationId);
    }

    /**
     * 把新上下文合并进已有上下文，同名键以新值为准。
     *
     * @param conversationId 对话 ID
     * @param newContext 待合并的上下文字段
     */
    public void mergeContext(String conversationId, Map<String, Object> newContext) {
        if (newContext == null || newContext.isEmpty()) {
            return;
        }

        Map<String, Object> currentContext = getContext(conversationId);
        currentContext.putAll(newContext);
        
        String key = CONTEXT_PREFIX + conversationId;
        redisTemplate.delete(key);
        redisTemplate.opsForHash().putAll(key, currentContext);
        
        log.debug("合并上下文: conversationId={}, size={}", conversationId, currentContext.size());
    }
}
