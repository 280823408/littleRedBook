package com.example.littleredbook.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class StringRedisClient {
    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;
    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    public StringRedisClient(StringRedisTemplate stringRedisTemplate, RedissonClient redissonClient) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.redissonClient = redissonClient;
    }

    /**
     * 根据键获取Redis中的字符串值
     * @param key 键
     * @return 对应的字符串值，如果键不存在返回null
     */
    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    /**
     * 根据键获取Redis中的值并转换为指定类型对象
     * @param key 键
     * @param type 目标对象类型
     * @param <T> 泛型类型
     * @return 转换后的对象，如果键不存在或值为空返回null
     */
    public <T> T get(String key, Class<T> type) {
        String json = get(key);
        return StrUtil.isNotBlank(json) ? JSONUtil.toBean(json, type) : null;
    }

    /**
     * 批量获取多个键对应的字符串值
     * @param keys 键列表
     * @return 对应值的列表，不存在的键对应位置为null
     */
    public List<String> multiGet(List<String> keys) {
        return stringRedisTemplate.opsForValue().multiGet(keys);
    }

    /**
     * 批量获取多个键的值并转换为指定类型对象列表
     * @param keys 键列表
     * @param type 目标对象类型
     * @param <T> 泛型类型
     * @return 转换后的对象列表，不存在的键对应位置为null
     */
    public <T> List<T> multiGet(List<String> keys, Class<T> type) {
        List<String> jsons = multiGet(keys);
        return jsons.stream()
                .map(json -> json != null ? JSONUtil.toBean(json, type) : null)
                .collect(Collectors.toList());
    }

    /**
     * 设置键值对，并指定过期时间
     * @param key 键
     * @param value 值对象，将被转换为JSON字符串存储
     * @param time 过期时间数值
     * @param unit 过期时间单位
     */
    public void set(String key, Object value, Long time, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value), time, unit);
    }

    /**
     * 仅在键不存在时设置值（SETNX命令）
     * @param key 键
     * @param value 值对象，将被转换为JSON字符串
     * @return true-设置成功，false-键已存在
     */
    public Boolean setIfAbsent(String key, Object value) {
        return stringRedisTemplate.opsForValue().setIfAbsent(key, JSONUtil.toJsonStr(value));
    }

    /**
     * 仅在键不存在时设置值，并指定过期时间
     * @param key 键
     * @param value 值对象，将被转换为JSON字符串
     * @param timeout 过期时间数值
     * @param unit 过期时间单位
     * @return true-设置成功，false-键已存在
     */
    public Boolean setIfAbsent(String key, Object value, long timeout, TimeUnit unit) {
        return stringRedisTemplate.opsForValue().setIfAbsent(key, JSONUtil.toJsonStr(value), timeout, unit);
    }

    /**
     * 批量设置多个键值对
     * @param map 键值对映射，值对象会被转换为JSON字符串
     */
    public void multiSet(Map<String, Object> map) {
        Map<String, String> stringMap = map.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> JSONUtil.toJsonStr(e.getValue())
                ));
        stringRedisTemplate.opsForValue().multiSet(stringMap);
    }

    /**
     * 删除指定键
     * @param key 键
     * @return true-删除成功，false-键不存在
     */
    public Boolean delete(String key) {
        return stringRedisTemplate.delete(key);
    }

    /**
     * 检查键是否存在
     * @param key 键
     * @return true-存在，false-不存在
     */
    public Boolean hasKey(String key) {
        return stringRedisTemplate.hasKey(key);
    }

    /**
     * 设置键的过期时间
     * @param key 键
     * @param time 过期时间数值
     * @param unit 过期时间单位
     * @return true-设置成功，false-键不存在或设置失败
     */
    public Boolean expire(String key, long time, TimeUnit unit) {
        return stringRedisTemplate.expire(key, time, unit);
    }

    /**
     * 获取键的剩余生存时间（默认单位：秒）
     * @param key 键
     * @return 剩余时间（秒），-1表示永久，-2表示键不存在
     */
    public Long getExpire(String key) {
        return stringRedisTemplate.getExpire(key);
    }

    /**
     * 获取键的剩余生存时间（指定单位）
     * @param key 键
     * @param unit 时间单位
     * @return 剩余时间，-1表示永久，-2表示键不存在
     */
    public Long getExpire(String key, TimeUnit unit) {
        return stringRedisTemplate.getExpire(key, unit);
    }

    /**
     * 将键的整数值递增1
     * @param key 键
     * @return 递增后的值
     */
    public Long increment(String key) {
        return stringRedisTemplate.opsForValue().increment(key);
    }

    /**
     * 将键的整数值递增指定步长
     * @param key 键
     * @param delta 递增步长
     * @return 递增后的值
     */
    public Long increment(String key, long delta) {
        return stringRedisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * 将键的整数值递减1
     * @param key 键
     * @return 递减后的值
     */
    public Long decrement(String key) {
        return stringRedisTemplate.opsForValue().decrement(key);
    }

    /**
     * 将键的整数值递减指定步长
     * @param key 键
     * @param delta 递减步长
     * @return 递减后的值
     */
    public Long decrement(String key, long delta) {
        return stringRedisTemplate.opsForValue().decrement(key, delta);
    }

    /**
     * 设置带逻辑过期时间的键值对（实际不自动过期，需配合逻辑过期策略使用）
     * @param key 键
     * @param value 值对象
     * @param time 逻辑过期时间数值
     * @param unit 时间单位
     */
    public void setWithLogicalExpire(String key, Object value, Long time, TimeUnit unit) {
        RedisData redisData = new RedisData();
        redisData.setData(value);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(unit.toSeconds(time)));
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisData));
    }

    /**
     * 缓存穿透保护查询：查询缓存，不存在时查数据库并写入缓存，数据库不存在时缓存空值
     * @param keyPrefix 缓存键前缀
     * @param id 业务ID
     * @param type 返回对象类型
     * @param dbFallback 数据库查询函数
     * @param time 缓存时间数值
     * @param unit 缓存时间单位
     * @param <R> 返回类型泛型
     * @param <ID> ID类型泛型
     * @return 查询结果，不存在时返回null
     */
    public <R, ID> R queryWithPassThrough(
            String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Long time, TimeUnit unit) {
        String key = keyPrefix + id;
        String json = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(json)) {
            return JSONUtil.toBean(json, type);
        }
        if (json != null) {
            return null;
        }
        R result = dbFallback.apply(id);
        if (result == null) {
            stringRedisTemplate.opsForValue().set(key, "", RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
            return null;
        }
        this.set(key, result, time, unit);
        return result;
    }

    /**
     * 逻辑过期缓存查询：查询缓存并判断逻辑过期时间，过期时获取互斥锁后异步重建
     * @param keyPrefix 缓存键前缀
     * @param id 业务ID
     * @param type 返回对象类型
     * @param dbFallback 数据库查询函数
     * @param time 逻辑过期时间数值
     * @param unit 时间单位
     * @param <R> 返回类型泛型
     * @param <ID> ID类型泛型
     * @return 查询结果（可能为过期数据），不存在缓存时返回null
     */
    public <R, ID> R queryWithLogicalExpire(
            String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Long time, TimeUnit unit) {
        String key = keyPrefix + id;
        String json = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isBlank(json)) {
            return null;
        }

        RedisData redisData = JSONUtil.toBean(json, RedisData.class);
        R result = JSONUtil.toBean((JSONObject) redisData.getData(), type);
        LocalDateTime expireTime = redisData.getExpireTime();
        if (expireTime.isAfter(LocalDateTime.now())) {
            return result;
        }
        String lockKey = RedisConstants.LOCK_PREFIX + id;
        RLock lock = redissonClient.getLock(lockKey);
        boolean isLock = lock.tryLock();
        if (isLock) {
            CACHE_REBUILD_EXECUTOR.submit(() -> {
                try {
                    R newResult = dbFallback.apply(id);
                    this.setWithLogicalExpire(key, newResult, time, unit);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    lock.unlock();
                }
            });
        }
        return result;
    }

    /**
     * 互斥锁缓存查询：缓存未命中时使用分布式锁保证单线程查询数据库并写入缓存
     * @param keyPrefix 缓存键前缀
     * @param id 业务ID
     * @param type 返回对象类型
     * @param dbFallback 数据库查询函数
     * @param time 缓存时间数值
     * @param unit 时间单位
     * @param <R> 返回类型泛型
     * @param <ID> ID类型泛型
     * @return 查询结果，不存在时返回null
     */
    public <R, ID> R queryWithMutex(
            String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Long time, TimeUnit unit) {
        String key = keyPrefix + id;
        String json = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(json)) {
            return JSONUtil.toBean(json, type);
        }
        if (json != null) {
            return null;
        }
        String lockKey = RedisConstants.LOCK_PREFIX + id;
        RLock lock = redissonClient.getLock(lockKey);
        R result;
        try {
            boolean isLock = lock.tryLock(100, 10, TimeUnit.SECONDS);
            if (!isLock) {
                Thread.sleep(50);
                return queryWithMutex(keyPrefix, id, type, dbFallback, time, unit);
            }

            result = dbFallback.apply(id);
            if (result == null) {
                stringRedisTemplate.opsForValue().set(key, "", RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
                return null;
            }
            this.set(key, result, time, unit);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
        return result;
    }
}
