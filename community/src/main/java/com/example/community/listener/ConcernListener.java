package com.example.community.listener;

import com.example.community.service.IConcernService;
import com.example.littleredbook.entity.Concern;
import com.example.littleredbook.utils.HashRedisClient;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import static com.example.littleredbook.utils.MQConstants.*;
import static com.example.littleredbook.utils.RedisConstants.CACHE_CONCERN_KEY;
import static com.example.littleredbook.utils.RedisConstants.CACHE_CONCERN_TTL;

@Component
@RequiredArgsConstructor
public class ConcernListener {
    private final IConcernService concernService;
    private final HashRedisClient hashRedisClient;
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = COMMUNITY_CONCERN_LIKE_QUEUE, durable = "true"),
            exchange = @Exchange(name = TOPIC_COMMUNITY_EXCHANGE, type = ExchangeTypes.TOPIC, delayed = "true"),
            key = {TOPIC_COMMUNITY_EXCHANGE_WITH_COMMUNITY_CONCERN_LIKE_QUEUE_ROUTING_KEY}
    ))
    public void listenLikeUser(Concern concern) {
        if (concern.getId() != null) {
            concernService.removeConcernById(concern.getId());
            return;
        }
        concernService.addConcern(concern);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = COMMUNITY_CONRERN_CACHE_ADD_QUEUE, durable = "true"),
            exchange = @Exchange(name = TOPIC_COMMUNITY_EXCHANGE, type = ExchangeTypes.TOPIC, delayed = "true"),
            key = {TOPIC_COMMUNITY_EXCHANGE_WITH_COMMUNITY_CONRERN_CACHE_ADD_QUEUE_ROUTING_KEY}
    ))
    public void listenConcernCacheAdd(Concern concern) {
        Integer id = concern.getId();
        hashRedisClient.hMultiSet(CACHE_CONCERN_KEY + id, Concern.class);
        hashRedisClient.expire(CACHE_CONCERN_KEY + id, CACHE_CONCERN_TTL, TimeUnit.MINUTES);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = COMMUNITY_CONRERN_CACHE_DELETE_QUEUE, durable = "true"),
            exchange = @Exchange(name = TOPIC_COMMUNITY_EXCHANGE, type = ExchangeTypes.TOPIC, delayed = "true"),
            key = {TOPIC_COMMUNITY_EXCHANGE_WITH_COMMUNITY_CONRERN_CACHE_DELETE_QUEUE_ROUTING_KEY}
    ))
    public void listenConcernCacheDelete(Integer id) {
        hashRedisClient.delete(CACHE_CONCERN_KEY + id);
    }
}
