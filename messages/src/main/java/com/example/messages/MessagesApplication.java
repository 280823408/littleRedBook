package com.example.messages;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
@EnableFeignClients
@SpringBootApplication
@MapperScan("com.example.messages.mapper")
@Import({com.example.littleredbook.config.RedissonConfig.class, com.example.littleredbook.utils.StringRedisClient.class,
        com.example.littleredbook.utils.HashRedisClient.class, com.example.littleredbook.config.MqConfig.class,
        com.example.littleredbook.config.ErrorMessageConfiguration.class, com.example.littleredbook.utils.MQClient.class,
        com.example.littleredbook.config.WebConfig.class})
public class MessagesApplication {
    public static void main(String[] args) {
        SpringApplication.run(MessagesApplication.class, args);
    }
}
