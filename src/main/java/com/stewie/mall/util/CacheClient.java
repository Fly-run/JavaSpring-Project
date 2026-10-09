package com.stewie.mall.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis 缓存工具类。用 StringRedisTemplate 存 JSON 字符串,把缓存三大经典问题
 * 的通用解法封装在这里,业务层直接复用:
 *
 * - 缓存穿透:查询 DB 结果为空时,也缓存一个空值(短 TTL),避免每次都打穿到数据库;
 *   对应 {@link #setEmpty}。
 * - 缓存击穿:热点 key 过期瞬间,用 {@link #tryLock}(SET NX EX)互斥锁,
 *   保证只有一个线程回源重建缓存。
 * - 缓存雪崩:所有 key 的 TTL 在基准值上随机浮动 ±20%,避免同一时刻大批量同时过期。
 */
@Component
public class CacheClient {

    /** 空值缓存标记:DB 里没有这条数据时,缓存这个空串(区别于 key 不存在的 null) */
    public static final String EMPTY_VALUE = "";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public CacheClient(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /** 序列化对象为 JSON 字符串,失败返回 null(不抛异常阻断主流程) */
    public String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    /** 反序列化 JSON 字符串为指定类型,失败返回 null */
    public <T> T fromJson(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (Exception e) {
            return null;
        }
    }

    /** 存对象缓存,TTL 在基准值上随机浮动防雪崩 */
    public void set(String key, Object value, long ttlSeconds) {
        String json = toJson(value);
        if (json == null) {
            return;
        }
        redisTemplate.opsForValue().set(key, json, randomTtl(ttlSeconds), TimeUnit.SECONDS);
    }

    /** 存空值缓存(防穿透),短 TTL */
    public void setEmpty(String key, long ttlSeconds) {
        redisTemplate.opsForValue().set(key, EMPTY_VALUE, randomTtl(ttlSeconds), TimeUnit.SECONDS);
    }

    /**
     * 取缓存的 JSON 字符串。
     * 返回 null = key 不存在(缓存未命中);返回 {@link #EMPTY_VALUE} = 命中空值缓存;其他 = 命中正常缓存。
     */
    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    /**
     * 互斥锁:SET key 1 NX EX ttlSeconds。原子操作,只有一个线程能抢到锁,
     * 用于缓存击穿时"单线程回源重建缓存"。
     */
    public boolean tryLock(String key, long ttlSeconds) {
        Boolean ok = redisTemplate.opsForValue().setIfAbsent(key, "1", ttlSeconds, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(ok);
    }

    public void unlock(String key) {
        redisTemplate.delete(key);
    }

    /** 基准 TTL 上下浮动 20%(最小 1 秒),让不同 key 的过期时间错开 */
    private long randomTtl(long baseSeconds) {
        long delta = Math.max(1, baseSeconds / 5);
        long offset = (long) (Math.random() * delta);
        return baseSeconds + offset;
    }
}
