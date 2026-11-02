package com.javaee.documentservice.collaborate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javaee.common.utils.RedisUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 协同编辑操作处理器，负责操作变换、Redis 缓存与逻辑时钟分配。
 *
 * <p>每个文档的操作按发生顺序缓存在 Redis 列表中，仅保留最近 500 条并保留 24 小时；
 * 逻辑时钟按「文档-用户」维度单调递增，用于判断操作先后与幂等。</p>
 */
@Component
public class EditOperationHandler {

    private static final Logger log = LoggerFactory.getLogger(EditOperationHandler.class);

    private static final String REDIS_OPS_PREFIX = "collaborate:ops:";
    private static final String REDIS_CLOCK_PREFIX = "collaborate:clock:";
    private static final int MAX_CACHED_OPS = 500;
    private static final long OPS_EXPIRE_HOURS = 24;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 依据本地已提交的较新操作变换远端操作，并写入操作缓存。
     *
     * @param remoteOp 远端提交的编辑操作
     * @param localUserId 执行变换的本地用户 ID
     * @return 位置修正后的编辑操作
     */
    public EditOperation transformAndApply(EditOperation remoteOp, String localUserId) {
        List<EditOperation> recentOps = getRecentOperations(remoteOp.getDocumentId());

        EditOperation transformedOp = remoteOp;
        for (EditOperation localOp : recentOps) {
            if (localOp.getUserId().equals(localUserId) && localOp.getClock() > remoteOp.getClock()) {
                transformedOp = transform(transformedOp, localOp);
            }
        }

        saveOperation(transformedOp);
        return transformedOp;
    }

    /**
     * 按操作变换规则修正远端操作的位置偏移。
     *
     * <p>仅处理「插入-插入」与「删除-插入」两种组合：当本地插入的起点不晚于远端操作时，
     * 远端操作的下标整体后移本地插入文本的长度；其余组合原样返回。</p>
     *
     * @param remoteOp 待变换的远端操作
     * @param localOp 已提交的本地操作
     * @return 变换后的操作；无需变换时返回原远端操作
     */
    public EditOperation transform(EditOperation remoteOp, EditOperation localOp) {
        if (remoteOp.getType() == EditOperation.OpType.INSERT && localOp.getType() == EditOperation.OpType.INSERT) {
            if (localOp.getFrom() <= remoteOp.getFrom()) {
                EditOperation transformed = new EditOperation();
                transformed.setOpId(remoteOp.getOpId());
                transformed.setDocumentId(remoteOp.getDocumentId());
                transformed.setUserId(remoteOp.getUserId());
                transformed.setUserName(remoteOp.getUserName());
                transformed.setType(EditOperation.OpType.INSERT);
                transformed.setFrom(remoteOp.getFrom() + localOp.getText().length());
                transformed.setTo(remoteOp.getTo() + localOp.getText().length());
                transformed.setText(remoteOp.getText());
                transformed.setClock(remoteOp.getClock());
                return transformed;
            }
        }

        if (remoteOp.getType() == EditOperation.OpType.DELETE && localOp.getType() == EditOperation.OpType.INSERT) {
            if (localOp.getFrom() <= remoteOp.getFrom()) {
                EditOperation transformed = new EditOperation();
                transformed.setOpId(remoteOp.getOpId());
                transformed.setDocumentId(remoteOp.getDocumentId());
                transformed.setUserId(remoteOp.getUserId());
                transformed.setUserName(remoteOp.getUserName());
                transformed.setType(EditOperation.OpType.DELETE);
                transformed.setFrom(remoteOp.getFrom() + localOp.getText().length());
                transformed.setTo(remoteOp.getTo() + localOp.getText().length());
                transformed.setText("");
                transformed.setClock(remoteOp.getClock());
                return transformed;
            }
        }

        return remoteOp;
    }

    /**
     * 将操作序列化后追加到文档的操作队列，并裁剪超出上限的历史。
     *
     * @param operation 待保存的编辑操作
     */
    public void saveOperation(EditOperation operation) {
        String opsKey = REDIS_OPS_PREFIX + operation.getDocumentId();
        try {
            String opJson = objectMapper.writeValueAsString(operation);
            redisUtils.lRightPush(opsKey, opJson);
            redisUtils.expire(opsKey, OPS_EXPIRE_HOURS, TimeUnit.HOURS);

            Long size = redisUtils.lSize(opsKey);
            if (size != null && size > MAX_CACHED_OPS) {
                redisUtils.lLeftPop(opsKey);
            }
        } catch (JsonProcessingException e) {
            log.error("序列化编辑操作失败: {}", operation.getOpId(), e);
        }
    }

    /**
     * 读取文档最近缓存的编辑操作，反序列化失败的单条操作会被跳过。
     *
     * @param documentId 文档 ID
     * @return 按写入顺序排列的编辑操作列表；无缓存时返回空列表
     */
    public List<EditOperation> getRecentOperations(String documentId) {
        String opsKey = REDIS_OPS_PREFIX + documentId;
        List<Object> rawList = redisUtils.lRange(opsKey, 0, MAX_CACHED_OPS);
        List<EditOperation> operations = new ArrayList<>();

        if (rawList != null) {
            for (Object obj : rawList) {
                try {
                    EditOperation op = objectMapper.readValue(obj.toString(), EditOperation.class);
                    operations.add(op);
                } catch (JsonProcessingException e) {
                    log.warn("反序列化编辑操作失败: {}", obj, e);
                }
            }
        }
        return operations;
    }

    /**
     * 分配并返回该用户在该文档下的下一个逻辑时钟。
     *
     * @param documentId 文档 ID
     * @param userId 用户 ID
     * @return 单调递增的逻辑时钟，Redis 计数不可用时返回 1
     */
    public long getNextClock(String documentId, String userId) {
        String clockKey = REDIS_CLOCK_PREFIX + documentId + ":" + userId;
        Long value = redisTemplate.opsForValue().increment(clockKey, 1);
        redisUtils.expire(clockKey, OPS_EXPIRE_HOURS, TimeUnit.HOURS);
        return value != null ? value : 1;
    }

    /**
     * 过滤出时钟晚于指定值的操作，供客户端增量拉取。
     *
     * @param documentId 文档 ID
     * @param sinceClock 客户端已持有的最大逻辑时钟
     * @return 时钟大于 {@code sinceClock} 的操作列表
     */
    public List<EditOperation> getOperationsSince(String documentId, long sinceClock) {
        List<EditOperation> allOps = getRecentOperations(documentId);
        List<EditOperation> result = new ArrayList<>();
        for (EditOperation op : allOps) {
            if (op.getClock() > sinceClock) {
                result.add(op);
            }
        }
        return result;
    }
}