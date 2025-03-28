package com.example.littleredbook.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.example.littleredbook.utils.MQConstants.*;

@Configuration
@ConditionalOnProperty(prefix = "spring.rabbitmq.listener.simple.retry", name = "enabled", havingValue = "true")
public class ErrorMessageConfiguration {
    @Bean
    public DirectExchange errorMessageExchange() {
        return new DirectExchange(DIRECT_ERROR_EXCHANGE);
    }

    @Bean
    public Queue errorMessageQueue() {
        return new Queue(ERROR_QUEUE, true);
    }

    @Bean
    public Binding errorMessageBinding() {
        return BindingBuilder.bind(errorMessageQueue()).to(errorMessageExchange()).with(DIRECT_ERROR_EXCHANGE_WITH_ERROR_QUEUE_ROUTING_KEY);
    }

    @Bean
    public MessageRecoverer repulishMessageRecoverer(RabbitTemplate rabbitTemplate) {
        return new RepublishMessageRecoverer(rabbitTemplate, DIRECT_ERROR_EXCHANGE, DIRECT_ERROR_EXCHANGE_WITH_ERROR_QUEUE_ROUTING_KEY);
    }
}
