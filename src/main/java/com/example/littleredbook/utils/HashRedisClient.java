package com.example.littleredbook.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class HashRedisClient {
    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;
    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);
    private final HashOperations<String, String, String> hashOperations;

    public HashRedisClient(StringRedisTemplate stringRedisTemplate, RedissonClient redissonClient) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.redissonClient = redissonClient;
        this.hashOperations = stringRedisTemplate.opsForHash();
    }

    /**
     * 获取Hash中指定字段的值
     * @param key 键
     * @param field 字段
     * @return 字段对应的值，不存在返回null
     */
    public String hGet(String key, String field) {
        return hashOperations.get(key, field);
    }

    /**
     * 获取Hash字段值并转换为指定类型
     * @param key 键
     * @param field 字段
     * @param type 目标类型
     * @return 转换后的对象，不存在或空值返回null
     */
    public <T> T hGet(String key, String field, Class<T> type) {
        String value = hGet(key, field);
        return StrUtil.isNotBlank(value) ? BeanUtil.toBean(value,type) : null;
    }

    /**
     * 批量获取Hash中多个字段的值
     * @param key 键
     * @param fields 字段列表
     * @return 字段值列表，按输入顺序返回
     */
    public List<String> hMultiGet(String key, List<String> fields) {
        return hashOperations.multiGet(key, fields);
    }

    /**
     * 批量获取并转换为指定类型
     * @param key 键
     * @param type 目标类型
     * @return 转换后的对象，不存在或空值返回null
     */
    public <T> T hMultiGet(String key, Class<T> type) throws ParseException {
        if (!hasKey(key)) {
            return null;
        }
        Field[] fields = type.getDeclaredFields();
        List<String> fieldNames = Arrays.stream(fields).map(Field::getName).collect(Collectors.toList());
        List<Type> fieldTypes = Arrays.stream(fields).map(Field::getGenericType).collect(Collectors.toList());
        List<String> values = this.hMultiGet(key, fieldNames);
        if (values == null || values.isEmpty()) {
            return null;
        }
        if (fieldNames.size() != values.size()) {
            throw new IllegalStateException("字段和值数量不匹配");
        }
        Map<String, Object> fieldValueMap = new HashMap<>();
        for (int i = 0; i < fieldNames.size(); i++) {
            String field = fieldNames.get(i);
            String value = values.get(i);
            Type fieldType = fieldTypes.get(i);
            Class<?> types = getRawType(fieldType);
            Object obj = null;
            if (types == Timestamp.class) {
                obj = DateUtils.StringToTimeStamp(value);
            } else if (isSimpleType(types)) {
                obj = toSimpleType(value, types);
            }else if (types == List.class) {
                Type genericType  = fields[i].getGenericType();
                ParameterizedType pType = (ParameterizedType) genericType;
                Type[] actualTypes = pType.getActualTypeArguments();
                if (actualTypes.length > 0) {
                    Type actualType = actualTypes[0];
                    if (actualType instanceof Class) {
                        types = (Class<?>) actualType;
                    }
                }
                obj = JSONUtil.toList(value, types);
            } else if (value != null && !value.equals("null")){
                obj = JSONUtil.toBean(value, types);
            }
            fieldValueMap.put(field, obj);
        }
        return BeanUtil.mapToBean(fieldValueMap, type, CopyOptions.create().setIgnoreError(true));
    }

    private Class<?> getRawType(Type type) {
        if (type instanceof Class) {
            return (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            ParameterizedType pType = (ParameterizedType) type;
            Type rawType = pType.getRawType();
            if (rawType instanceof Class) {
                return (Class<?>) rawType;
            }
        }
        throw new IllegalArgumentException("Unsupported type: " + type);
    }

    /**
     * 设置Hash字段值
     * @param key 键
     * @param field 字段
     * @param value 值（自动JSON序列化）
     * @param time 过期时间
     * @param unit 时间单位
     */
    public void hSet(String key, String field, Object value, Long time, TimeUnit unit) {
        if (isSimpleType(value)) {
            hashOperations.put(key, field, String.valueOf(value));
            stringRedisTemplate.expire(key, time, unit);
            return;
        }
        hashOperations.put(key, field, JSONUtil.toJsonStr(value));
        stringRedisTemplate.expire(key, time, unit);
    }

    /**
     * 字段不存在时设置值
     * @param key 键
     * @param field 字段
     * @param value 值
     * @return true-设置成功
     */
    public Boolean hSetIfAbsent(String key, String field, Object value) {
        return hashOperations.putIfAbsent(key, field, value.toString());
    }

    /**
     * 批量设置多个字段值
     * @param key 键
     * @param bean 类对象
     */
    public void hMultiSet(String key, Object bean) {
        Map<String, String> map = new HashMap<>();
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object obj = field.get(bean);
                String str = "";
                if (isSimpleType(obj)) {
                    str = String.valueOf(obj);
                } else  if (obj.getClass() == Timestamp.class){
                    str = DateUtils.TimeStampToString((Timestamp) obj);
                } else {
                    str = JSONUtil.toJsonStr(obj);
                }
                map.put(field.getName(), str);
            } catch (IllegalAccessException e) {
                log.error("字段访问异常", e);
            }
        }
        hashOperations.putAll(key, map);
    }

    private boolean isSimpleType(Object type) {
        return type == null ||
                type instanceof Number ||
                type instanceof String ||
                type instanceof Boolean ||
                type instanceof Character ||
                type.getClass().isPrimitive();
    }

    private boolean isSimpleType(Class type) {
        return type == null || type == Integer.class || type == String.class
                || type == Long.class || type == Boolean.class || type == Float.class
                || type == Double.class;
    }

    private Object toSimpleType(String str, Class type) {
        Object obj = null;
        if (str == null || str.equals("null")) return null;
        if (type == Integer.class) {
            obj = Integer.parseInt(str);
        } else if (type == String.class) {
            obj = str;
        } else if (type == Long.class) {
            obj = Long.parseLong(str);
        } else if (type == Boolean.class) {
            obj = Boolean.parseBoolean(str);
        } else if (type == Float.class) {
            obj = Float.parseFloat(str);
        } else if (type == Double.class) {
            obj = Double.parseDouble(str);
        }
        return obj;
    }

    /**
     * 删除一个或多个字段
     * @param key 键
     * @param fields 字段数组
     * @return 删除的字段数量
     */
    public Long hDelete(String key, String... fields) {
        return hashOperations.delete(key, Arrays.stream(fields).toArray());
    }

    /**
     * 检查字段是否存在
     * @param key 键
     * @param field 字段
     * @return true-存在
     */
    public Boolean hExists(String key, String field) {
        return hashOperations.hasKey(key, field);
    }

    /**
     * 获取所有字段名
     * @param key 键
     * @return 字段集合
     */
    public Set<String> hKeys(String key) {
        return hashOperations.keys(key);
    }

    /**
     * 获取所有字段值
     * @param key 键
     * @return 值列表
     */
    public List<String> hValues(String key) {
        return hashOperations.values(key);
    }

    /**
     * 数值递增
     * @param key 键
     * @param field 字段
     * @param delta 增量
     * @return 递增后的值
     */
    public Long hIncrement(String key, String field, long delta) {
        return hashOperations.increment(key, field, delta);
    }

    /**
     * 数值递减
     * @param key 键
     * @param field 字段
     * @param delta 减量
     * @return 递减后的值
     */
    public Long hDecrement(String key, String field, long delta) {
        return hashOperations.increment(key, field, -delta);
    }


    /**
     * 设置过期时间
     * @param key
     * @param time
     * @param unit
     * @return true-设置成功
     */
    public Boolean expire(String key, long time, TimeUnit unit) {
        return stringRedisTemplate.expire(key, time, unit);
    }

    /**
     * 获取过期时间
     * @param key
     * @return 过期时间（秒），不存在返回null
     */
    public Long getExpire(String key) {
        return stringRedisTemplate.getExpire(key);
    }

    /**
     * 获取过期时间
     * @param key
     * @param unit
     * @return 过期时间，不存在返回null
     */
    public Long getExpire(String key, TimeUnit unit) {
        return stringRedisTemplate.getExpire(key, unit);
    }

    /**
     * 删除键
     * @param key
     * @return true-删除成功
     */
    public Boolean delete(String key) {
        return stringRedisTemplate.delete(key);
    }

    /**
     * 查询键是否存在
     * @param key
     * @return true-存在
     */
    public Boolean hasKey(String key) {
        return stringRedisTemplate.hasKey(key);
    }

    /**
     * 设置带逻辑过期时间的字段值
     * @param key 键
     * @param field 字段
     * @param value 值
     * @param time 过期时间
     * @param unit 时间单位
     */
    public void hSetWithLogicalExpire(String key, String field, Object value, Long time, TimeUnit unit) {
        RedisData redisData = new RedisData();
        redisData.setData(value);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(unit.toSeconds(time)));
        hashOperations.put(key, field, JSONUtil.toJsonStr(redisData));
    }

//    /**
//     * 缓存穿透保护查询：查询缓存，不存在时查数据库并写入缓存，数据库不存在时缓存空值
//     * @param keyPrefix 缓存键前缀
//     * @param id 业务ID
//     * @param type 返回对象类型
//     * @param dbFallback 数据库查询函数
//     * @param time 缓存时间数值
//     * @param unit 缓存时间单位
//     * @param <R> 返回类型泛型
//     * @param <ID> ID类型泛型
//     * @return 查询结果，不存在时返回null
//     */
//    public <R, ID> R queryWithPassThrough(
//            String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Long time, TimeUnit unit) {
//        String key = keyPrefix + id;
//        Field[] filed = type.getDeclaredFields();
//        List<String> fields = new ArrayList<>();
//        for (Field field : filed) {
//            fields.add(field.getName());
//        }
//        List<String> json = this.hMultiGet(key, fields);
//        if (!json.isEmpty()) {
//            return JSONUtil.toBean(json.toString(), type);
//        }
//        if (json != null) {
//            return null;
//        }
//        R result = dbFallback.apply(id);
//        if (result == null) {
//            this.set(key, "", RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
//            return null;
//        }
//        this.set(key, result, time, unit);
//        return result;
//    }
//
//    /**
//     * 逻辑过期缓存查询：查询缓存并判断逻辑过期时间，过期时获取互斥锁后异步重建
//     * @param keyPrefix 缓存键前缀
//     * @param id 业务ID
//     * @param type 返回对象类型
//     * @param dbFallback 数据库查询函数
//     * @param time 逻辑过期时间数值
//     * @param unit 时间单位
//     * @param <R> 返回类型泛型
//     * @param <ID> ID类型泛型
//     * @return 查询结果（可能为过期数据），不存在缓存时返回null
//     */
//    public <R, ID> R queryWithLogicalExpire(
//            String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Long time, TimeUnit unit) {
//        String key = keyPrefix + id;
//        String json = this.get(key);
//        if (StrUtil.isBlank(json)) {
//            return null;
//        }
//
//        RedisData redisData = JSONUtil.toBean(json, RedisData.class);
//        R result = JSONUtil.toBean((JSONObject) redisData.getData(), type);
//        LocalDateTime expireTime = redisData.getExpireTime();
//        if (expireTime.isAfter(LocalDateTime.now())) {
//            return result;
//        }
//        String lockKey = RedisConstants.LOCK_PREFIX + id;
//        RLock lock = redissonClient.getLock(lockKey);
//        boolean isLock = lock.tryLock();
//        if (isLock) {
//            CACHE_REBUILD_EXECUTOR.submit(() -> {
//                try {
//                    R newResult = dbFallback.apply(id);
//                    this.setWithLogicalExpire(key, newResult, time, unit);
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                } finally {
//                    lock.unlock();
//                }
//            });
//        }
//        return result;
//    }
//
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
            String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Long time, TimeUnit unit) throws ParseException {
        String key = keyPrefix + id;
        R result = this.hMultiGet(key, type);
        boolean isLock = false;
        if (result != null) {
            return result;
        }
        String lockKey = RedisConstants.LOCK_HASH_PREFIX + id;
        RLock lock = redissonClient.getLock(lockKey);
        try {
            isLock = lock.tryLock(1, 5, TimeUnit.SECONDS);
            if (!isLock) {
                Thread.sleep(50);
                return queryWithMutex(keyPrefix, id, type, dbFallback, time, unit);
            }
            result = dbFallback.apply(id);
            if (result == null) {
                this.hMultiSet(key, "");
                return null;
            }
            this.hMultiSet(key, result);
            this.expire(key, time, unit);
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
