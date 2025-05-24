package com.example.messages.listener;

import com.example.littleredbook.entity.LikeComment;
import com.example.littleredbook.utils.HashRedisClient;
import com.example.messages.service.ILikeCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import static com.example.littleredbook.utils.MQConstants.*;
import static com.example.littleredbook.utils.RedisConstants.CACHE_LIKECOMMENT_KEY;
import static com.example.littleredbook.utils.RedisConstants.CACHE_LIKECOMMENT_TTL;

@Component
@RequiredArgsConstructor
public class LikeCommentListener {
    private final ILikeCommentService likeCommentService;
    private final HashRedisClient hashRedisClient;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MESSAGES_LIKECOMMENT_LIKE_QUEUE, durable = "true"),
            exchange = @Exchange(name = TOPIC_MESSAGES_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = {TOPIC_MESSAGES_EXCHANGE_WITH_MESSAGES_LIKECOMMENT_LIKE_QUEUE_ROUTING_KEY}
    ))
    public void listenLikeComment(LikeComment likeComment) {
        if (likeComment.getId() != null) {
            likeCommentService.removeLikeComment(likeComment.getId());
            return;
        }
        likeCommentService.addLikeComment(likeComment);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MESSAGES_LIKECOMMENT_CACHE_ADD_QUEUE, durable = "true"),
            exchange = @Exchange(name = TOPIC_MESSAGES_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = {TOPIC_MESSAGES_EXCHANGE_WITH_MESSAGES_LIKECOMMENT_CACHE_ADD_QUEUE_ROUTING_KEY}
    ))
    public void listenLikeCommentCacheAdd(LikeComment likeComment) {
        Integer id = likeComment.getId();
        hashRedisClient.hMultiSet(CACHE_LIKECOMMENT_KEY + id, likeComment);
        hashRedisClient.expire(CACHE_LIKECOMMENT_KEY + id, CACHE_LIKECOMMENT_TTL, TimeUnit.MINUTES);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MESSAGES_LIKECOMMENT_CACHE_DELETE_QUEUE, durable = "true"),
            exchange = @Exchange(name = TOPIC_MESSAGES_EXCHANGE, type = ExchangeTypes.TOPIC, delayed = "true"),
            key = {TOPIC_MESSAGES_EXCHANGE_WITH_MESSAGES_LIKECOMMENT_CACHE_DELETE_QUEUE_ROUTING_KEY}
    ))
    public void listenLikeCommentCacheDelete(Integer id) {
        hashRedisClient.delete(CACHE_LIKECOMMENT_KEY + id);
    }
}
