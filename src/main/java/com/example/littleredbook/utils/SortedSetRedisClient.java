package com.example.littleredbook.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Redis有序集合操作增强客户端
 *
 * <p>功能说明：
 * 1. 封装ZSet数据结构的核心操作<br>
 * 2. 实现带分数排序的业务场景支持<br>
 * 3. 集成分布式锁保障缓存重建安全<br>
 * 4. 支持对象自动序列化与反序列化<br>
 * 5. 提供缓存穿透保护机制<br>
 *
 * <p>典型场景：
 * - 实时排行榜数据维护<br>
 * - 时间轴/分数范围查询<br>
 * - 延迟任务队列实现<br>
 * - 热点数据排序缓存<br>
 * - 分布式会话管理<br>
 *
 * @author Mike
 * @since 2025/3/1
 */
@Slf4j
@Component
public class SortedSetRedisClient {
    @Data
    public static class ScoredValue<T> {
        private final T value;
        private final Double score;
        public ScoredValue(T value, Double score) {
            this.value = value;
            this.score = score;
        }
    }
    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;
    private final ZSetOperations<String, String> zSetOperations;
    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    public SortedSetRedisClient(StringRedisTemplate stringRedisTemplate, RedissonClient redissonClient) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.redissonClient = redissonClient;
        this.zSetOperations = stringRedisTemplate.opsForZSet();
    }

    /**
     * 添加元素到有序集合
     * @param key 键
     * @param value 值对象
     * @param score 分数
     * @return 是否成功添加新元素（忽略已存在元素的更新）
     */
    public Boolean add(String key, Object value, double score) {
        return zSetOperations.add(key, StrUtil.toString(value) , score);
    }

    /**
     * 批量添加元素（相同key）
     * @param key 键
     * @param values 值-分数对集合
     * @return 添加成功的元素数量
     */
    public Long multiAdd(String key, Set<ZSetOperations.TypedTuple<String>> values) {
        return zSetOperations.add(key, values);
    }

    /**
     * 获取有序集合元素数量
     * @param key 键
     * @return 元素总数
     */
    public Long zCard(String key) {
        return zSetOperations.zCard(key);
    }

    /**
     * 获取分数范围内的元素数量
     * @param key 键
     * @param min 最小分数
     * @param max 最大分数
     * @return 元素数量
     */
    public Long count(String key, double min, double max) {
        return zSetOperations.count(key, min, max);
    }

    /**
     * 增加元素的分数
     * @param key 键
     * @param value 值对象
     * @param delta 增量
     * @return 更新后的分数
     */
    public Double incrementScore(String key, Object value, double delta) {
        return zSetOperations.incrementScore(key, StrUtil.toString(value), delta);
    }

    /**
     * 获取元素的分数
     * @param key 键
     * @param value 值对象
     * @return 分数值（不存在返回null）
     */
    public Double score(String key, Object value) {
        return zSetOperations.score(key, StrUtil.toString(value));
    }

    /**
     * 获取元素排名（从低到高）
     * @param key 键
     * @param value 值对象
     * @return 排名（从0开始）
     */
    public Long rank(String key, Object value) {
        return zSetOperations.rank(key, StrUtil.toString(value));
    }

    /**
     * 获取元素反向排名（从高到低）
     * @param key 键
     * @param value 值对象
     * @return 排名（从0开始）
     */
    public Long reverseRank(String key, Object value) {
        return zSetOperations.reverseRank(key, StrUtil.toString(value));
    }

    /**
     * 移除元素
     * @param key 键
     * @param values 值对象列表
     * @return 成功移除的数量
     */
    public Long remove(String key, Object... values) {
        String[] value = Arrays.stream(values)
                .map(StrUtil::toString)
                .toArray(String[]::new);
        return zSetOperations.remove(key, value);
    }

    /**
     * 按分数范围获取元素（升序）
     * @param key 键
     * @param min 最小分数
     * @param max 最大分数
     * @param type 目标类型
     * @return 反序列化后的对象列表
     */
    public <T> List<T> rangeByScore(String key, double min, double max, Class<T> type) {
        return this.deserializeValues(zSetOperations.rangeByScore(key, min, max), type);
    }

    /**
     * 按排名范围获取元素（升序）
     * @param key 键
     * @param start 开始索引
     * @param end 结束索引
     * @param type 目标类型
     * @return 反序列化后的对象列表
     */
    public <T> List<T> range(String key, long start, long end, Class<T> type) {
        return this.deserializeValues(zSetOperations.range(key, start, end), type);
    }

    /**
     * 按分数范围获取元素（带分数）
     * @param key 键
     * @param min 最小分数
     * @param max 最大分数
     * @param type 目标类型
     * @return 包含分数值的对象列表
     */
    public <T> List<ScoredValue<T>> rangeByScoreWithScores(
            String key, double min, double max, Class<T> type) {
        return convertToScoredValues(
                zSetOperations.rangeByScoreWithScores(key, min, max), type);
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
     * 转化对象列表
     * @param values
     * @param type
     * @return
     * @param <T>
     */
    private <T> List<T> deserializeValues(Set<String> values, Class<T> type) {
        if (values == null) return Collections.emptyList();
        return values.stream()
                .map(value -> BeanUtil.toBean(value, type))
                .collect(Collectors.toList());
    }

    /**
     * 转换为带分数的对象列表
     * @param tuples
     * @param type
     * @return
     * @param <T>
     */
    private <T> List<ScoredValue<T>> convertToScoredValues(
            Set<ZSetOperations.TypedTuple<String>> tuples, Class<T> type) {
        return tuples.stream()
                .map(t -> new ScoredValue<>(
                        BeanUtil.toBean(t.getValue(), type),
                        t.getScore()))
                .collect(Collectors.toList());
    }

    /**
     * 互斥锁缓存查询：缓存未命中时使用分布式锁保证单线程查询数据库并写入缓存
     * @param key 缓存键
     * @param type 返回对象类型
     * @param dbFallback 数据库查询函数
     * @param time 缓存时间数值
     * @param unit 时间单位
     * @param <R> 返回类型泛型
     * @return 查询结果，不存在时返回null
     */
    public <R> List<R> queryWithMutex(
            String key, Object queryParams, Class<R> type, Function<Object, Map<R, Double>> dbFallback, Long time, TimeUnit unit) {
        List<R> results = this.rangeByScore(key, Double.MIN_VALUE, Double.MAX_VALUE, type);
        if (!results.isEmpty()) {
            return results;
        }
        if (this.containsPlaceholder(key)) {
            return Collections.emptyList();
        }
        String lockKey = RedisConstants.LOCK_PREFIX + key;
        RLock lock = redissonClient.getLock(lockKey);
        try {
            boolean isLocked = lock.tryLock(100, 60, TimeUnit.SECONDS);
            if (!isLocked) {
                Thread.sleep(30);
                return queryWithMutex(key, queryParams, type, dbFallback, time, unit);
            }
            results = this.rangeByScore(key, Double.MIN_VALUE, Double.MAX_VALUE, type);
            if (!key.isEmpty()) {
                return results;
            }
            Map<R, Double> dbResult = dbFallback.apply(queryParams);
            if (dbResult.isEmpty()) {
                this.add(key, "EMPTY_PLACEHOLDER", -Double.MAX_VALUE);
                this.expire(key, RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
                return Collections.emptyList();
            }
            Set<ZSetOperations.TypedTuple<String>> tuples = dbResult.entrySet().stream()
                    .map(entry -> new DefaultTypedTuple<>(
                            JSONUtil.toJsonStr(entry.getKey()),
                            entry.getValue()))
                    .collect(Collectors.toSet());
            this.multiAdd(key, tuples);
            this.expire(key, time, unit);
            return new ArrayList<>(dbResult.keySet());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Lock acquisition interrupted", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private Boolean containsPlaceholder(String key) {
        return this.rangeByScore(key, -Double.MAX_VALUE, -Double.MAX_VALUE, String.class)
                .stream()
                .anyMatch("EMPTY_PLACEHOLDER"::equals);
    }
}
