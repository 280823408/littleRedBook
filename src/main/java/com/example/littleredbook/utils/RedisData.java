package com.example.littleredbook.utils;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Redis缓存数据包装类
 *
 * <p>功能说明：
 * 1. 实现逻辑过期时间管理机制<br>
 * 2. 支持缓存数据与过期时间统一封装<br>
 * 3. 为缓存续期功能提供基础数据结构<br>
 * 4. 支持不同类型缓存数据的统一处理<br>
 *
 * <p>典型场景：
 * - 逻辑过期缓存方案实现<br>
 * - 热点数据缓存时间动态维护<br>
 * - 缓存击穿保护策略<br>
 * - 分布式缓存数据版本管理<br>
 *
 * @author Mike
 * @since 2025/2/25
 */
@Data
public class RedisData {
    /** 逻辑过期时间（应当晚于实际Redis TTL） */
    private LocalDateTime expireTime;
    /**
     * 实际存储的业务数据
     * <p>支持类型：
     * - 简单值类型（String/Number）<br>
     * - 复杂对象（自动JSON序列化）<br>
     * - 集合类型（List/Set/Map）<br>
     */
    private Object data;
}
