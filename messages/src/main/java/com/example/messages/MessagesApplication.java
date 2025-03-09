package com.example.messages;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import java.util.TimeZone;

@SpringBootApplication
@MapperScan("com.example.messages.mapper")
@Import({com.example.littleredbook.config.RedissonConfig.class, com.example.littleredbook.utils.StringRedisClient.class,
        com.example.littleredbook.utils.HashRedisClient.class})
public class MessagesApplication {
    public static void main(String[] args) {
        SpringApplication.run(MessagesApplication.class, args);
    }

}
