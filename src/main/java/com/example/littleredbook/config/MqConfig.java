package com.example.littleredbook.config;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Slf4j
@Configuration
//@AllArgsConstructor
public class MqConfig {
//    private final RabbitTemplate rabbitTemplate;
    @Bean
    public MessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
//    @PostConstruct
//    public void init() {
//        rabbitTemplate.setReturnsCallback(returnedMessage -> {
//            log.error("触发return callback,");
//            log.debug("exchange: {}", returnedMessage.getExchange());
//            log.debug("routingKey: {}", returnedMessage.getRoutingKey());
//            log.debug("message: {}", returnedMessage.getMessage());
//            log.debug("replyCode: {}", returnedMessage.getReplyCode());
//            log.debug("replyText: {}", returnedMessage.getReplyText());
//        });
//    }
}
