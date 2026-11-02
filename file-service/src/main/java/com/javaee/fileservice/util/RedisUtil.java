package com.javaee.fileservice.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis 工具类，封装字符串键的读写、删除、过期与计数操作。
 */
@Component
public class RedisUtil {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 写入缓存，不设置过期时间。
     *
     * @param key 缓存键
     * @param value 缓存值
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 写入缓存并设置过期时间。
     *
     * @param key 缓存键
     * @param value 缓存值
     * @param timeout 过期时长
     * @param unit 过期时长的单位
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * 读取缓存值。
     *
     * @param key 缓存键
     * @return 缓存值；键不存在时返回 null
     */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 删除缓存键。
     *
     * @param key 缓存键
     * @return 删除成功返回 true，键不存在时返回 false
     */
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    /**
     * 判断缓存键是否存在。
     *
     * @param key 缓存键
     * @return 存在返回 true，否则返回 false
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 重置缓存键的过期时间。
     *
     * @param key 缓存键
     * @param timeout 过期时长
     * @param unit 过期时长的单位
     * @return 设置成功返回 true，键不存在时返回 false
     */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }

    /**
     * 将缓存值原子自增 1。
     *
     * @param key 缓存键
     * @return 自增后的值
     */
    public Long increment(String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    /**
     * 将缓存值原子自增指定步长。
     *
     * @param key 缓存键
     * @param delta 自增步长
     * @return 自增后的值
     */
    public Long increment(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * 将缓存值原子自减 1。
     *
     * @param key 缓存键
     * @return 自减后的值
     */
    public Long decrement(String key) {
        return redisTemplate.opsForValue().decrement(key);
    }

    /**
     * 将缓存值原子自减指定步长。
     *
     * @param key 缓存键
     * @param delta 自减步长
     * @return 自减后的值
     */
    public Long decrement(String key, long delta) {
        return redisTemplate.opsForValue().decrement(key, delta);
    }
}
