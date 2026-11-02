package com.javaee.documentservice.collaborate;

import com.javaee.common.utils.RedisUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 协同会话管理器，维护文档与在线用户的对应关系。
 *
 * <p>本地内存保存当前实例的会话，Redis 保存全局会话并以 30 分钟过期兜底；
 * 读取在线用户时优先取本地集合，本地为空再回落到 Redis。</p>
 */
@Component
public class CollaborateSessionManager {

    private static final Logger log = LoggerFactory.getLogger(CollaborateSessionManager.class);

    private static final String REDIS_DOC_USERS_PREFIX = "collaborate:doc:users:";
    private static final String REDIS_USER_DOC_PREFIX = "collaborate:user:doc:";
    private static final long SESSION_TIMEOUT_MINUTES = 30;

    private final Map<String, Set<String>> localDocumentUsers = new ConcurrentHashMap<>();
    private final Map<String, String> localUserDocuments = new ConcurrentHashMap<>();

    @Autowired
    private RedisUtils redisUtils;

    /**
     * 记录用户加入文档协同，并刷新会话过期时间。
     *
     * @param documentId 文档 ID
     * @param userId 加入的用户 ID
     * @param userName 加入的用户名
     */
    public void userJoinDocument(String documentId, String userId, String userName) {
        localDocumentUsers.computeIfAbsent(documentId, k -> ConcurrentHashMap.newKeySet()).add(userId);
        localUserDocuments.put(userId, documentId);

        String redisKey = REDIS_DOC_USERS_PREFIX + documentId;
        redisUtils.hSet(redisKey, userId, userName);
        redisUtils.expire(redisKey, SESSION_TIMEOUT_MINUTES, TimeUnit.MINUTES);

        String userDocKey = REDIS_USER_DOC_PREFIX + userId;
        redisUtils.set(userDocKey, documentId, SESSION_TIMEOUT_MINUTES, TimeUnit.MINUTES);

        log.info("用户加入文档协同: userId={}, userName={}, documentId={}, 当前在线人数={}",
                userId, userName, documentId, getOnlineUserCount(documentId));
    }

    /**
     * 记录用户离开文档协同，并清理本地与 Redis 中的会话记录。
     *
     * @param documentId 文档 ID
     * @param userId 离开的用户 ID
     */
    public void userLeaveDocument(String documentId, String userId) {
        Set<String> users = localDocumentUsers.get(documentId);
        if (users != null) {
            users.remove(userId);
            if (users.isEmpty()) {
                localDocumentUsers.remove(documentId);
            }
        }
        localUserDocuments.remove(userId);

        String redisKey = REDIS_DOC_USERS_PREFIX + documentId;
        redisUtils.hDelete(redisKey, userId);

        log.info("用户离开文档协同: userId={}, documentId={}, 当前在线人数={}",
                userId, documentId, getOnlineUserCount(documentId));
    }

    /**
     * 查询文档当前的在线用户。
     *
     * @param documentId 文档 ID
     * @return 在线用户 ID 集合；无人在线时返回空集合
     */
    public Set<String> getDocumentOnlineUsers(String documentId) {
        Set<String> localUsers = localDocumentUsers.get(documentId);
        if (localUsers != null && !localUsers.isEmpty()) {
            return localUsers;
        }

        Map<Object, Object> redisUsers = redisUtils.hGetAll(REDIS_DOC_USERS_PREFIX + documentId);
        if (redisUsers != null) {
            Set<String> userIds = ConcurrentHashMap.newKeySet();
            for (Object key : redisUsers.keySet()) {
                userIds.add(key.toString());
            }
            return userIds;
        }
        return Set.of();
    }

    /**
     * 统计文档当前的在线人数，仅统计本实例内的会话。
     *
     * @param documentId 文档 ID
     * @return 在线人数，无会话时返回 0
     */
    public int getOnlineUserCount(String documentId) {
        Set<String> localUsers = localDocumentUsers.get(documentId);
        if (localUsers != null) {
            return localUsers.size();
        }
        return 0;
    }

    /**
     * 查询用户当前所在的文档，本地未命中时回落到 Redis。
     *
     * @param userId 用户 ID
     * @return 文档 ID；用户不在任何协同会话中时返回 {@code null}
     */
    public String getUserCurrentDocument(String userId) {
        String docId = localUserDocuments.get(userId);
        if (docId != null) {
            return docId;
        }
        Object redisDocId = redisUtils.get(REDIS_USER_DOC_PREFIX + userId);
        return redisDocId != null ? redisDocId.toString() : null;
    }

    /**
     * 续期文档会话的过期时间，避免长时间协作中途失效。
     *
     * @param documentId 文档 ID
     */
    public void refreshSession(String documentId) {
        String redisKey = REDIS_DOC_USERS_PREFIX + documentId;
        redisUtils.expire(redisKey, SESSION_TIMEOUT_MINUTES, TimeUnit.MINUTES);
    }
}