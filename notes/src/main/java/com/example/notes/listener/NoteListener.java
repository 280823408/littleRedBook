package com.example.notes.listener;

import com.example.littleredbook.utils.HashRedisClient;
import com.example.notes.dto.LikeMessage;
import com.example.notes.service.INoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import static com.example.littleredbook.utils.MQConstants.*;
import static com.example.littleredbook.utils.RedisConstants.*;

@Component
@RequiredArgsConstructor
public class NoteListener {
    private final INoteService noteService;
    private final HashRedisClient hashRedisClient;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = NOTES_NOTE_CACHE_QUEUE, durable = "true"),
            exchange = @Exchange(name = TOPIC_NOTES_EXCHANGE, type = ExchangeTypes.TOPIC, delayed = "true"),
            key = {TOPIC_NOTES_EXCHANGE_WITH_NOTES_NOTE_CACHE_QUEUE_ROUTING_KEY}
    ))
    public void listenLikeNoteCache(LikeMessage likeMessage) {
        hashRedisClient.hIncrement(CACHE_NOTE_KEY + likeMessage.getId(), "likeNum", likeMessage.getDelta());
        hashRedisClient.expire(CACHE_NOTE_KEY + likeMessage.getId(), CACHE_NOTE_TTL, TimeUnit.MINUTES);
    }
}
