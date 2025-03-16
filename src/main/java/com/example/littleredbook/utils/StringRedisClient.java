package com.example.littleredbook.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Redis字符串操作增强客户端
 *
 * <p>功能说明：
 * 1. 封装String数据结构的核心操作<br>
 * 2. 实现对象自动序列化与反序列化<br>
 * 3. 集成缓存穿透/雪崩/击穿防护策略<br>
 * 4. 支持逻辑过期时间维护热点数据<br>
 * 5. 提供分布式环境下的互斥锁机制<br>
 *
 * <p>典型场景：
 * - 用户会话状态管理<br>
 * - 热点数据缓存加速<br>
 * - 分布式原子计数器实现<br>
 * - 系统配置项集中存储<br>
 * - 高并发查询请求防护<br>
 *
 * @author Mike
 * @since 2025/2/25
 */
@Slf4j
@Component
public class StringRedisClient {
    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;
    private final ValueOperations<String, String> valueOperations;
    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    public StringRedisClient(StringRedisTemplate stringRedisTemplate, RedissonClient redissonClient) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.redissonClient = redissonClient;
        this.valueOperations = stringRedisTemplate.opsForValue();
    }

    /**
     * 根据键获取Redis中的字符串值
     * @param key 键
     * @return 对应的字符串值，如果键不存在返回null
     */
    public String get(String key) {
        return valueOperations.get(key);
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
        return valueOperations.multiGet(keys);
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
        valueOperations.set(key, JSONUtil.toJsonStr(value), time, unit);
    }

    /**
     * 仅在键不存在时设置值（SETNX命令）
     * @param key 键
     * @param value 值对象，将被转换为JSON字符串
     * @return true-设置成功，false-键已存在
     */
    public Boolean setIfAbsent(String key, Object value) {
        return valueOperations.setIfAbsent(key, JSONUtil.toJsonStr(value));
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
        return valueOperations.setIfAbsent(key, JSONUtil.toJsonStr(value), timeout, unit);
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
        valueOperations.multiSet(stringMap);
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
        return valueOperations.increment(key);
    }

    /**
     * 将键的整数值递增指定步长
     * @param key 键
     * @param delta 递增步长
     * @return 递增后的值
     */
    public Long increment(String key, long delta) {
        return valueOperations.increment(key, delta);
    }

    /**
     * 将键的整数值递减1
     * @param key 键
     * @return 递减后的值
     */
    public Long decrement(String key) {
        return valueOperations.decrement(key);
    }

    /**
     * 将键的整数值递减指定步长
     * @param key 键
     * @param delta 递减步长
     * @return 递减后的值
     */
    public Long decrement(String key, long delta) {
        return valueOperations.decrement(key, delta);
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
        valueOperations.set(key, JSONUtil.toJsonStr(redisData));
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
        String json = this.get(key);
        if (StrUtil.isNotBlank(json)) {
            return JSONUtil.toBean(json, type);
        }
        if (json != null) {
            return null;
        }
        R result = dbFallback.apply(id);
        if (result == null) {
            this.set(key, "", RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
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
        String json = this.get(key);
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
        String json = this.get(key);
        boolean isLock = false;
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
            isLock = lock.tryLock(1, 5, TimeUnit.SECONDS);
            if (!isLock) {
                Thread.sleep(50);
                return queryWithMutex(keyPrefix, id, type, dbFallback, time, unit);
            }
            result = dbFallback.apply(id);
            if (result == null) {
                this.set(key, "", RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
                return null;
            }
            this.set(key, result, time, unit);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (isLock && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        return result;
    }

    /**
     * 互斥锁缓存查询（列表版本）：缓存未命中时使用分布式锁保证单线程查询数据库并写入缓存
     * @param keyPrefix 缓存键前缀
     * @param id 业务ID
     * @param type 列表元素类型
     * @param dbFallback 数据库批量查询函数
     * @param time 缓存时间数值
     * @param unit 时间单位
     * @param <R> 返回列表元素类型泛型
     * @param <ID> ID类型泛型
     * @return 查询结果列表，不存在时返回空列表
     */
    public <R, ID> List<R> queryListWithMutex(String keyPrefix, ID id, Class<R> type, Function<ID, List<R>> dbFallback, Long time, TimeUnit unit) {
        String key = keyPrefix + id;
        String json = this.get(key);
        boolean isLock = false;
        if (StrUtil.isNotBlank(json)) {
            return JSONUtil.toList(json, type);
        }
        if (json != null) {
            return Collections.emptyList();
        }
        String lockKey = RedisConstants.LOCK_PREFIX + key;
        RLock lock = redissonClient.getLock(lockKey);
        List<R> result;
        try {
            isLock = lock.tryLock(1, 5, TimeUnit.SECONDS);
            if (!isLock) {
                Thread.sleep(50);
                return this.queryListWithMutex(keyPrefix, id, type, dbFallback, time, unit);
            }
            json = valueOperations.get(key);
            if (StrUtil.isNotBlank(json)) {
                return JSONUtil.toList(json, type);
            }
            if (json != null) {
                return Collections.emptyList();
            }
            result = dbFallback.apply(id);
            if (result == null || result.isEmpty()) {
                this.set(key, "[]", RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
                return Collections.emptyList();
            }
            this.set(key, result, time, unit);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (isLock && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        return result;
    }

    /**
     * 双参数版缓存查询方法
     * @param keyPattern 缓存键模式（需含两个占位符，如 "cache:message:chat:%s:%s"）
     * @param type 返回类型
     * @param dbFallback 数据库回查函数（接收两个参数）
     * @param time 缓存时间
     * @param unit 时间单位
     * @param param1 参数1
     * @param param2 参数2
     */
    public <R> List<R> queryListWithMutex(String keyPattern, Class<R> type,
                                          BiFunction<Integer, Integer, List<R>> dbFallback,
                                          Long time, TimeUnit unit,
                                          Integer param1, Integer param2) {
        String key = String.format(keyPattern, param1, param2);
        String json = this.get(key);
        boolean isLock = false;
        if (StrUtil.isNotBlank(json)) {
            return JSONUtil.toList(json, type);
        }
        if (json != null) {
            return Collections.emptyList();
        }
        String lockKey = RedisConstants.LOCK_PREFIX + key;
        RLock lock = redissonClient.getLock(lockKey);
        List<R> result;
        try {
            isLock = lock.tryLock(1, 5, TimeUnit.SECONDS);
            if (!isLock) {
                Thread.sleep(50);
                return queryListWithMutex(keyPattern, type, dbFallback, time, unit, param1, param2);
            }
            json = valueOperations.get(key);
            if (StrUtil.isNotBlank(json)) {
                return JSONUtil.toList(json, type);
            }
            if (json != null) {
                return Collections.emptyList();
            }
            result = dbFallback.apply(param1, param2);
            if (result == null || result.isEmpty()) {
                this.set(key, "[]", RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
                return Collections.emptyList();
            }
            this.set(key, result, time, unit);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (isLock && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        return result;
    }
}
