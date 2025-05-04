package com.example.notes;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableFeignClients
@SpringBootApplication
@MapperScan("com.example.notes.mapper")
@Import({com.example.littleredbook.config.RedissonConfig.class, com.example.littleredbook.utils.StringRedisClient.class,
        com.example.littleredbook.utils.HashRedisClient.class, com.example.littleredbook.config.MqConfig.class,
        com.example.littleredbook.config.ErrorMessageConfiguration.class, com.example.littleredbook.utils.MQClient.class,
        com.example.littleredbook.config.WebConfig.class})
public class NotesApplication {
    public static void main(String[] args) {
        SpringApplication.run(NotesApplication.class, args);
    }
}
