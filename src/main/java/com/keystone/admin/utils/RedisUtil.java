package com.keystone.admin.utils;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis 工具：对 RedisTemplate 的常用操作封装
 */
@Component
public final class RedisUtil {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    // ==================== Key ====================

    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public void del(String... keys) {
        if (keys == null || keys.length == 0) {
            return;
        }
        if (keys.length == 1) {
            redisTemplate.delete(keys[0]);
        } else {
            redisTemplate.delete(Arrays.asList(keys));
        }
    }

    public boolean expire(String key, long time, TimeUnit unit) {
        return time > 0 && Boolean.TRUE.equals(redisTemplate.expire(key, time, unit));
    }

    public long getExpire(String key) {
        Long expire = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        return expire == null ? 0L : expire;
    }

    // ==================== String ====================

    public Object get(String key) {
        return key == null ? null : redisTemplate.opsForValue().get(key);
    }

    public boolean set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean set(String key, Object value, long time, TimeUnit unit) {
        try {
            if (time > 0) {
                redisTemplate.opsForValue().set(key, value, time, unit);
            } else {
                set(key, value);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ==================== Hash ====================

    public Object hget(String key, String field) {
        return redisTemplate.opsForHash().get(key, field);
    }

    public Map<Object, Object> hmget(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    public boolean hset(String key, String field, Object value) {
        try {
            redisTemplate.opsForHash().put(key, field, value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void hdel(String key, Object... fields) {
        redisTemplate.opsForHash().delete(key, fields);
    }

    // ==================== List ====================

    public List<Object> lGet(String key, long start, long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    public boolean rpush(String key, Object value) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ==================== Set ====================

    public Set<Object> sGet(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    public long sAdd(String key, Object... values) {
        Long n = redisTemplate.opsForSet().add(key, values);
        return n == null ? 0L : n;
    }

    // ==================== Sorted Set ====================

    public boolean zAdd(String key, Object value, double score) {
        Boolean ok = redisTemplate.opsForZSet().add(key, value, score);
        return Boolean.TRUE.equals(ok);
    }

    public Set<Object> zReverseRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().reverseRange(key, start, end);
    }

    // ==================== 通用 ====================

    public long incr(String key, long delta) {
        Long n = redisTemplate.opsForValue().increment(key, delta);
        return n == null ? 0L : n;
    }

    public long getExpiresTime(String key, TimeUnit unit) {
        Long n = redisTemplate.opsForValue().getOperations().getExpire(key, unit);
        return n == null ? 0L : n;
    }

    public void multiSet(Map<String, Object> map) {
        redisTemplate.opsForValue().multiSet(map);
    }

    public List<Object> multiGet(Collection<String> keys) {
        return redisTemplate.opsForValue().multiGet(keys);
    }
}