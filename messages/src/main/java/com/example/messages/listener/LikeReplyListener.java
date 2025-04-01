package com.example.messages.listener;

import com.example.littleredbook.entity.LikeReply;
import com.example.littleredbook.utils.HashRedisClient;
import com.example.messages.service.ILikeReplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import static com.example.littleredbook.utils.MQConstants.*;
import static com.example.littleredbook.utils.RedisConstants.CACHE_LIKEREPLY_KEY;
import static com.example.littleredbook.utils.RedisConstants.CACHE_LIKEREPLY_TTL;

@Component
@RequiredArgsConstructor
public class LikeReplyListener {
    private final ILikeReplyService likeReplyService;
    private final HashRedisClient hashRedisClient;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MESSAGES_LIKEREPLY_LIKE_QUEUE, durable = "true"),
            exchange = @Exchange(name = TOPIC_MESSAGES_EXCHANGE, type = ExchangeTypes.TOPIC, delayed = "true"),
            key = {TOPIC_MESSAGES_EXCHANGE_WITH_MESSAGES_LIKEREPLY_LIKE_QUEUE_ROUTING_KEY}
    ))
    public void listenLikeNote(LikeReply likeReply) {
        if (likeReply.getId() != null) {
            likeReplyService.removeLikeReply(likeReply.getId());
            return;
        }
        likeReplyService.addLikeReply(likeReply);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MESSAGES_LIKEREPLY_CACHE_ADD_QUEUE, durable = "true"),
            exchange = @Exchange(name = TOPIC_MESSAGES_EXCHANGE, type = ExchangeTypes.TOPIC, delayed = "true"),
            key = {TOPIC_MESSAGES_EXCHANGE_WITH_MESSAGES_LIKEREPLY_CACHE_ADD_QUEUE_ROUTING_KEY}
    ))
    public void listenLikeNoteCacheAdd(LikeReply likeReply) {
        Integer id = likeReply.getId();
        hashRedisClient.hMultiSet(CACHE_LIKEREPLY_KEY + id, likeReply);
        hashRedisClient.expire(CACHE_LIKEREPLY_KEY + id, CACHE_LIKEREPLY_TTL, TimeUnit.MINUTES);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MESSAGES_LIKEREPLY_CACHE_DELETE_QUEUE, durable = "true"),
            exchange = @Exchange(name = TOPIC_MESSAGES_EXCHANGE, type = ExchangeTypes.TOPIC, delayed = "true"),
            key = {TOPIC_MESSAGES_EXCHANGE_WITH_MESSAGES_LIKEREPLY_CACHE_DELETE_QUEUE_ROUTING_KEY}
   ))
    public void listenLikeNoteCacheDelete(Integer id) {
        hashRedisClient.delete(CACHE_LIKEREPLY_KEY + id);
    }
}
