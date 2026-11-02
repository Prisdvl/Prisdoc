package com.javaee.common.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis 操作工具，封装字符串、Hash、List、Set、ZSet 的常用读写。
 *
 * <p>基于 {@link RedisTemplate} 操作，键与值的序列化策略由 RedisConfig 统一配置。</p>
 *
 * @author qxk
 */
@Component
public class RedisUtils {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 设置缓存，不指定过期时间。
     *
     * @param key 缓存键
     * @param value 缓存值
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 设置缓存并指定过期时间。
     *
     * @param key 缓存键
     * @param value 缓存值
     * @param timeout 过期时长
     * @param unit 过期时长单位
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * 获取缓存值。
     *
     * @param key 缓存键
     * @return 缓存值；键不存在时返回 null
     */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 删除单个缓存键。
     *
     * @param key 缓存键
     */
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    /**
     * 批量删除缓存键。
     *
     * @param keys 缓存键集合
     */
    public void delete(Collection<String> keys) {
        redisTemplate.delete(keys);
    }

    /**
     * 判断缓存键是否存在。
     *
     * @param key 缓存键
     * @return true-键存在
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 设置缓存键的过期时间。
     *
     * @param key 缓存键
     * @param timeout 过期时长
     * @param unit 过期时长单位
     * @return true-设置成功
     */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }

    /**
     * 获取缓存键的剩余过期时间。
     *
     * @param key 缓存键
     * @param unit 返回的时间单位
     * @return 剩余过期时间；键不存在时返回 -2
     */
    public Long getExpire(String key, TimeUnit unit) {
        return redisTemplate.getExpire(key, unit);
    }

    /**
     * 写入 Hash 字段。
     *
     * @param key 缓存键
     * @param hashKey Hash 字段名
     * @param value 字段值
     */
    public void hSet(String key, String hashKey, Object value) {
        redisTemplate.opsForHash().put(key, hashKey, value);
    }

    /**
     * 读取 Hash 字段。
     *
     * @param key 缓存键
     * @param hashKey Hash 字段名
     * @return 字段值；字段不存在时返回 null
     */
    public Object hGet(String key, String hashKey) {
        return redisTemplate.opsForHash().get(key, hashKey);
    }

    /**
     * 读取 Hash 的全部字段。
     *
     * @param key 缓存键
     * @return 全部字段键值对；键不存在时返回空 Map
     */
    public Map<Object, Object> hGetAll(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * 删除 Hash 的一个或多个字段。
     *
     * @param key 缓存键
     * @param hashKeys 待删除的 Hash 字段名
     */
    public void hDelete(String key, Object... hashKeys) {
        redisTemplate.opsForHash().delete(key, hashKeys);
    }

    /**
     * 判断 Hash 字段是否存在。
     *
     * @param key 缓存键
     * @param hashKey Hash 字段名
     * @return true-字段存在
     */
    public Boolean hHasKey(String key, String hashKey) {
        return redisTemplate.opsForHash().hasKey(key, hashKey);
    }

    /**
     * 向 List 左侧插入元素。
     *
     * @param key 缓存键
     * @param value 元素值
     */
    public void lLeftPush(String key, Object value) {
        redisTemplate.opsForList().leftPush(key, value);
    }

    /**
     * 向 List 右侧插入元素。
     *
     * @param key 缓存键
     * @param value 元素值
     */
    public void lRightPush(String key, Object value) {
        redisTemplate.opsForList().rightPush(key, value);
    }

    /**
     * 从 List 左侧弹出一个元素。
     *
     * @param key 缓存键
     * @return 弹出的元素；List 为空时返回 null
     */
    public Object lLeftPop(String key) {
        return redisTemplate.opsForList().leftPop(key);
    }

    /**
     * 从 List 右侧弹出一个元素。
     *
     * @param key 缓存键
     * @return 弹出的元素；List 为空时返回 null
     */
    public Object lRightPop(String key) {
        return redisTemplate.opsForList().rightPop(key);
    }

    /**
     * 获取 List 指定索引范围内的元素。
     *
     * @param key 缓存键
     * @param start 起始索引，从 0 开始
     * @param end 结束索引，包含该位置
     * @return 元素列表；区间为空时返回空列表
     */
    public List<Object> lRange(String key, long start, long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    /**
     * 获取 List 长度。
     *
     * @param key 缓存键
     * @return List 元素个数；键不存在时返回 0
     */
    public Long lSize(String key) {
        return redisTemplate.opsForList().size(key);
    }

    /**
     * 向 Set 添加一个或多个成员。
     *
     * @param key 缓存键
     * @param values 待添加的成员
     * @return 实际新增的成员数量
     */
    public Long sAdd(String key, Object... values) {
        return redisTemplate.opsForSet().add(key, values);
    }

    /**
     * 从 Set 移除一个或多个成员。
     *
     * @param key 缓存键
     * @param values 待移除的成员
     * @return 实际移除的成员数量
     */
    public Long sRemove(String key, Object... values) {
        return redisTemplate.opsForSet().remove(key, values);
    }

    /**
     * 获取 Set 的全部成员。
     *
     * @param key 缓存键
     * @return 成员集合；键不存在时返回空集合
     */
    public Set<Object> sMembers(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * 判断成员是否属于 Set。
     *
     * @param key 缓存键
     * @param value 待判断的成员
     * @return true-成员存在
     */
    public Boolean sIsMember(String key, Object value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }

    /**
     * 获取 Set 成员数量。
     *
     * @param key 缓存键
     * @return 成员数量；键不存在时返回 0
     */
    public Long sSize(String key) {
        return redisTemplate.opsForSet().size(key);
    }

    /**
     * 向 ZSet 添加成员并指定分数。
     *
     * @param key 缓存键
     * @param value 成员值
     * @param score 排序分数
     * @return true-成员为新增而非覆盖
     */
    public Boolean zAdd(String key, Object value, double score) {
        return redisTemplate.opsForZSet().add(key, value, score);
    }

    /**
     * 从 ZSet 移除一个或多个成员。
     *
     * @param key 缓存键
     * @param values 待移除的成员
     * @return 实际移除的成员数量
     */
    public Long zRemove(String key, Object... values) {
        return redisTemplate.opsForZSet().remove(key, values);
    }

    /**
     * 按索引范围获取 ZSet 成员。
     *
     * @param key 缓存键
     * @param start 起始索引，从 0 开始
     * @param end 结束索引，包含该位置
     * @return 成员集合；区间为空时返回空集合
     */
    public Set<Object> zRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().range(key, start, end);
    }

    /**
     * 按分数区间获取 ZSet 成员。
     *
     * @param key 缓存键
     * @param min 最小分数，包含该值
     * @param max 最大分数，包含该值
     * @return 成员集合；区间为空时返回空集合
     */
    public Set<Object> zRangeByScore(String key, double min, double max) {
        return redisTemplate.opsForZSet().rangeByScore(key, min, max);
    }

    /**
     * 获取 ZSet 成员数量。
     *
     * @param key 缓存键
     * @return 成员数量；键不存在时返回 0
     */
    public Long zSize(String key) {
        return redisTemplate.opsForZSet().size(key);
    }

    /**
     * 按模式匹配缓存键。
     *
     * @param pattern 匹配模式，如 user:*
     * @return 匹配到的键集合
     */
    public Set<String> keys(String pattern) {
        return redisTemplate.keys(pattern);
    }
}
