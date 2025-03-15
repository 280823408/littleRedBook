package com.example.search;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;

@EnableAspectJAutoProxy(exposeProxy = true)
@EnableFeignClients
@SpringBootApplication
@MapperScan("com.example.search.mapper")
@Import({com.example.littleredbook.config.RedissonConfig.class, com.example.littleredbook.utils.StringRedisClient.class,
        com.example.littleredbook.utils.HashRedisClient.class})
public class SearchApplication {
    public static void main(String[] args) {
        SpringApplication.run(SearchApplication.class, args);
    }

}
