package com.example.littleredbook.config;

import feign.Feign;
import feign.Request;
import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfiguration {
    @Bean
    public Request.Options feignOptions() {
        return new Request.Options(50000, 50000);
    }
    @Bean
    public Feign.Builder feignBuilder(Request.Options options) {
        return Feign.builder()
                .options(options)
                .retryer(new Retryer.Default(100, 1000, 3)); // 可选：设置重试策略
    }
}
