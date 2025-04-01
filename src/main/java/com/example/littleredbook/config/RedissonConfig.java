package com.example.littleredbook.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson 客户端配置类
 *
 * <p>功能说明：
 * 1. 配置Redisson客户端连接参数<br>
 * 2. 创建RedissonClient单例实例<br>
 * 3. 支持分布式锁、分布式对象等Redis高级功能<br>
 *<br>
 * <p>主要配置项：
 * - Redis服务器地址<br>
 * - 连接密码<br>
 * - 数据库编号<br>
 * - 连接池配置（可扩展）<br>
 *<br>
 * <p>安全规范：
 * - 生产环境密码应通过配置中心管理<br>
 * - 建议使用SSL加密连接<br>
 * - 限制Redis端口的网络访问<br>
 *
 * @author Mike
 * @since 2025/2/23
 */
@Configuration
public class RedissonConfig {

    /**
     * 创建Redisson客户端实例（单节点模式）
     *
     * <p>核心配置：
     * 1. 单节点服务器地址（redis://{host}:{port}）<br>
     * 2. 数据库密码认证<br>
     * 3. 指定数据库编号<br>
     * 4. 默认连接池配置<br>
     *<br>
     * <p>配置参数说明：
     * - address: Redis连接地址（协议前缀必须为redis://或rediss://）<br>
     * - password: 数据库密码（空密码需显式设置为null）<br>
     * - database: 选择数据库（0-15）<br>
     * - connectionPoolSize: 最大连接数（默认64）<br>
     * - connectTimeout: 连接超时时间（默认10000ms）<br>
     *
     * @return 配置完成的Redisson客户端实例
     */
    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://192.168.23.138:6379")
                .setPassword("123456")
                .setDatabase(5)
                .setConnectionPoolSize(64)
                .setConnectTimeout(3000)
                .setIdleConnectionTimeout(60000)
                .setRetryAttempts(3);
        return Redisson.create(config);
    }
}
