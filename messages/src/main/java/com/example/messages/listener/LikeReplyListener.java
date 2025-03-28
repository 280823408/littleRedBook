package com.example.messages.listener;

import com.example.littleredbook.entity.LikeReply;
import com.example.messages.service.ILikeReplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static com.example.littleredbook.utils.MQConstants.*;

@Component
@RequiredArgsConstructor
public class LikeReplyListener {
    private final ILikeReplyService likeReplyService;
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MESSAGES_LIKEREPLY_QUEUE, durable = "true"),
            exchange = @Exchange(name = TOPIC_MESSAGES_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = {TOPIC_MESSAGES_EXCHANGE_WITH_MESSAGES_LIKEREPLY_QUEUE_ROUTING_KEY}
    ))
    public void listenLikeNote(LikeReply likeReply) {
        if (likeReply.getId() != null) {
            likeReplyService.removeLikeReply(likeReply.getId());
            return;
        }
        likeReplyService.addLikeReply(likeReply);
    }
}
