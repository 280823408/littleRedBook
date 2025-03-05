package com.example.community;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
@EnableFeignClients
@SpringBootApplication
@MapperScan("com.example.community.mapper")
@Import({com.example.littleredbook.config.RedissonConfig.class, com.example.littleredbook.utils.StringRedisClient.class,
        com.example.littleredbook.utils.HashRedisClient.class})
public class CommunityApplication {
    public static void main(String[] args) {
        SpringApplication.run(CommunityApplication.class, args);
    }
}
