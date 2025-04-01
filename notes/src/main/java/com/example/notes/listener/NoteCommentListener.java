package com.example.notes.listener;

import com.example.littleredbook.entity.NoteComment;
import com.example.littleredbook.utils.HashRedisClient;
import com.example.notes.dto.LikeMessage;
import com.example.notes.dto.NoteCommentDTO;
import com.example.notes.service.INoteCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import static com.example.littleredbook.utils.MQConstants.*;
import static com.example.littleredbook.utils.RedisConstants.CACHE_COMMENT_KEY;
import static com.example.littleredbook.utils.RedisConstants.CACHE_COMMENT_TTL;

@Component
@RequiredArgsConstructor
public class NoteCommentListener {
    private final INoteCommentService noteCommentService;
    private final HashRedisClient hashRedisClient;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = NOTES_COMMENT_CACHE_LIKE_QUEUE, durable = "true"),
            exchange = @Exchange(name = TOPIC_NOTES_EXCHANGE, type = ExchangeTypes.TOPIC, delayed = "true"),
            key = {TOPIC_NOTES_EXCHANGE_WITH_NOTES_COMMENT_CACHE_LIKE_QUEUE_ROUTING_KEY}
    ))
    public void listenLikeCommentCacheLike(LikeMessage likeMessage) {
        hashRedisClient.hIncrement(CACHE_COMMENT_KEY + likeMessage.getId(), "likeNum", likeMessage.getDelta());
        hashRedisClient.expire(CACHE_COMMENT_KEY + likeMessage.getId(), CACHE_COMMENT_TTL, TimeUnit.MINUTES);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = NOTES_COMMENT_CACHE_ADD_QUEUE, durable = "true"),
            exchange = @Exchange(name = TOPIC_NOTES_EXCHANGE, type = ExchangeTypes.TOPIC, delayed = "true"),
            key = {TOPIC_NOTES_EXCHANGE_WITH_NOTES_COMMENT_CACHE_ADD_QUEUE_ROUTING_KEY}
    ))
    public void listenCommentCacheAdd(NoteCommentDTO noteComment) {
        Integer id = noteComment.getId();
        hashRedisClient.hMultiSet(CACHE_COMMENT_KEY + id, noteComment);
        hashRedisClient.expire(CACHE_COMMENT_KEY + id, CACHE_COMMENT_TTL, TimeUnit.MINUTES);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = NOTES_COMMENT_CACHE_DELETE_QUEUE, durable = "true"),
            exchange = @Exchange(name = TOPIC_NOTES_EXCHANGE, type = ExchangeTypes.TOPIC, delayed = "true"),
            key = {TOPIC_NOTES_EXCHANGE_WITH_NOTES_COMMENT_CACHE_DELETE_QUEUE_ROUTING_KEY}
    ))
    public void listenCommentCacheDelete(Integer id) {
        hashRedisClient.delete(CACHE_COMMENT_KEY + id);
    }
}
