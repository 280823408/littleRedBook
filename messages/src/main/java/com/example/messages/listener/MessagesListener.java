package com.example.messages.listener;

import com.example.littleredbook.entity.Message;
import com.example.littleredbook.utils.HashRedisClient;
import com.example.messages.service.IMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import static com.example.littleredbook.utils.MQConstants.*;
import static com.example.littleredbook.utils.RedisConstants.CACHE_MESSAGE_KEY;
import static com.example.littleredbook.utils.RedisConstants.CACHE_MESSAGE_TTL;

@Component
@RequiredArgsConstructor
public class MessagesListener {
    private final IMessageService messageService;
    private final HashRedisClient hashRedisClient;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MESSAGES_MESSAGE_CACHE_ADD_QUEUE, durable = "true"),
            exchange = @Exchange(name = TOPIC_MESSAGES_EXCHANGE, type = ExchangeTypes.TOPIC, delayed = "true"),
            key = {TOPIC_MESSAGES_EXCHANGE_WITH_MESSAGES_MESSAGE_CACHE_ADD_QUEUE_ROUTING_KEY}
    ))
    public void listenMessageCacheAdd(Message message) {
        Integer id = message.getId();
        hashRedisClient.hMultiSet(CACHE_MESSAGE_KEY + id, message);
        hashRedisClient.expire(CACHE_MESSAGE_KEY + id, CACHE_MESSAGE_TTL, TimeUnit.MINUTES);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MESSAGES_MESSAGE_CACHE_DELETE_QUEUE, durable = "true"),
            exchange = @Exchange(name = TOPIC_MESSAGES_EXCHANGE, type = ExchangeTypes.TOPIC, delayed = "true"),
            key = {TOPIC_MESSAGES_EXCHANGE_WITH_MESSAGES_MESSAGE_CACHE_DELETE_QUEUE_ROUTING_KEY}
    ))
    public void listenMessageCacheDelete(Integer id) {
        hashRedisClient.delete(CACHE_MESSAGE_KEY + id);
    }
}
