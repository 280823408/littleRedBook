package com.example.messages.listener;

import com.example.littleredbook.entity.LikeNote;
import com.example.littleredbook.utils.HashRedisClient;
import com.example.messages.service.ILikeNoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import static com.example.littleredbook.utils.MQConstants.*;
import static com.example.littleredbook.utils.RedisConstants.CACHE_LIKENOTE_KEY;
import static com.example.littleredbook.utils.RedisConstants.CACHE_LIKENOTE_TTL;

@Component
@RequiredArgsConstructor
public class LikeNoteListener {
    private final ILikeNoteService likeNoteService;
    private final HashRedisClient hashRedisClient;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MESSAGES_LIKENOTE_LIKE_QUEUE, durable = "true"),
            exchange = @Exchange(name = TOPIC_MESSAGES_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = {TOPIC_MESSAGES_EXCHANGE_WITH_MESSAGES_LIKENOTE_LIKE_QUEUE_ROUTING_KEY}
    ))
    public void listenLikeNote(LikeNote likeNote) {
        if (likeNote.getId() != null) {
            likeNoteService.removeLikeNote(likeNote.getId());
            return;
        }
        likeNoteService.addLikeNote(likeNote);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MESSAGES_LIKENOTE_CACHE_ADD_QUEUE, durable = "true"),
            exchange = @Exchange(name = TOPIC_MESSAGES_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = {TOPIC_MESSAGES_EXCHANGE_WITH_MESSAGES_LIKENOTE_CACHE_ADD_QUEUE_ROUTING_KEY}
    ))
    public void listenLikeNoteCacheAdd(LikeNote likeNote) {
        Integer id = likeNote.getId();
        hashRedisClient.hMultiSet(CACHE_LIKENOTE_KEY + id, likeNote);
        hashRedisClient.expire(CACHE_LIKENOTE_KEY + id, CACHE_LIKENOTE_TTL, TimeUnit.MINUTES);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MESSAGES_LIKENOTE_CACHE_DELETE_QUEUE, durable = "true"),
            exchange = @Exchange(name = TOPIC_MESSAGES_EXCHANGE, type = ExchangeTypes.TOPIC, delayed = "true"),
            key = {TOPIC_MESSAGES_EXCHANGE_WITH_MESSAGES_LIKENOTE_CACHE_DELETE_QUEUE_ROUTING_KEY}
    ))
    public void listenLikeNoteCacheDelete(Integer id) {
        hashRedisClient.delete(CACHE_LIKENOTE_KEY + id);
    }
}
