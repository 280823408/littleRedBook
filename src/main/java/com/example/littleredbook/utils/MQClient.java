package com.example.littleredbook.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MQClient {
    private final RabbitTemplate rabbitTemplate;

    public MQClient(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendMessage(String exchange, String routingKey, Object msg) {
        try {
            log.info("正在向交换机: {}, 路由键: {} 发送消息", exchange, routingKey);
            rabbitTemplate.convertAndSend(exchange, routingKey, msg);
            log.info("消息已成功发送到交换机: {}, 路由键: {}", exchange, routingKey);
        } catch (Exception e) {
            log.error("发送消息到交换机: {}, 路由键: {} 时失败", exchange, routingKey, e);
            throw new RuntimeException("发送消息失败", e);
        }
    }

    public void sendDelayMessage(String exchange, String routingKey, Object msg, int delay) {
        try {
            log.info("正在向交换机: {}, 路由键: {} 发送延迟消息, 延迟时间: {}ms", exchange, routingKey, delay);
            rabbitTemplate.convertAndSend(exchange, routingKey, msg, message -> {
                message.getMessageProperties().setDelay(delay);
                return message;
            });
            log.info("延迟消息已成功发送到交换机: {}, 路由键: {}, 延迟时间: {}ms", exchange, routingKey, delay);
        } catch (Exception e) {
            log.error("发送延迟消息到交换机: {}, 路由键: {}, 延迟时间: {}ms 时失败", exchange, routingKey, delay, e);
            throw new RuntimeException("发送延迟消息失败", e);
        }
    }

    public void sendMessageWithConfirm(String exchange, String routingKey, Object msg, int maxRetries) {
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.info("消息发送成功，关联数据: {}", correlationData);
            } else {
                log.error("消息发送失败，关联数据: {}, 原因: {}", correlationData, cause);
            }
        });
        int attempt = 0;
        boolean sent = false;
        while (attempt < maxRetries && !sent) {
            try {
                CorrelationData correlationData = new CorrelationData();
                rabbitTemplate.convertAndSend(exchange, routingKey, msg, correlationData);
                sent = true;
                log.info("消息已发送到交换机: {}, 路由键: {}", exchange, routingKey);
            } catch (Exception e) {
                attempt++;
                log.warn("发送消息尝试 {} 失败，正在重试...", attempt);
                if (attempt >= maxRetries) {
                    log.error("在 {} 次尝试后，消息发送仍然失败", maxRetries, e);
                }
            }
        }
    }
}
