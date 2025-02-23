package com.example.littleredbook.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {
    /**
     * 配置并创建 RedissonClient 实例。
     * 该实例用于连接指定的 Redis 单节点服务器。
     *
     * @return RedissonClient 返回配置好的 Redisson 客户端实例
     */
    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://192.168.23.131:6379").
                setPassword("123456").setDatabase(5);
        return Redisson.create(config);
    }
}
