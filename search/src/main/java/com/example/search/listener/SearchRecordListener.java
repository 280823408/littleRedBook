package com.example.search.listener;

import com.example.littleredbook.entity.SearchRecord;
import com.example.littleredbook.utils.HashRedisClient;
import com.example.search.service.ISearchRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import static com.example.littleredbook.utils.MQConstants.*;
import static com.example.littleredbook.utils.RedisConstants.CACHE_SEARCH_KEY;
import static com.example.littleredbook.utils.RedisConstants.CACHE_SEARCH_TTL;

@Component
@RequiredArgsConstructor
public class SearchRecordListener {
    private final ISearchRecordService searchRecordService;
    private final HashRedisClient hashRedisClient;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = SEARCH_SEARCHRECORD_CACHE_ADD_QUEUE, durable = "true"),
            exchange = @Exchange(name = TOPIC_SEARCH_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = {TOPIC_SEARCH_EXCHANGE_WITH_SEARCH_SEARCHRECORD_CACHE_ADD_QUEUE_ROUTING_KEY}
    ))
    public void listenSearchRecordCacheAdd(SearchRecord searchRecord) {
        Integer id = searchRecord.getId();
        hashRedisClient.hMultiSet(CACHE_SEARCH_KEY + id, SearchRecord.class);
        hashRedisClient.expire(CACHE_SEARCH_KEY + id, CACHE_SEARCH_TTL, TimeUnit.MINUTES);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = SEARCH_SEARCHRECORD_CACHE_DELETE_QUEUE, durable = "true"),
            exchange = @Exchange(name = TOPIC_SEARCH_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = {TOPIC_SEARCH_EXCHANGE_WITH_SEARCH_SEARCHRECORD_CACHE_DELETE_QUEUE_ROUTING_KEY}
    ))
    public void listenSearchRecordCacheDelete(Integer id) {
        hashRedisClient.delete(CACHE_SEARCH_KEY + id);
    }
}
