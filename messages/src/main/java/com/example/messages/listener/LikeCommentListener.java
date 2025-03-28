package com.example.messages.listener;

import com.example.littleredbook.entity.LikeComment;
import com.example.messages.service.ILikeCommentService;
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
public class LikeCommentListener {
    private final ILikeCommentService likeCommentService;
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MESSAGES_LIKECOMMENT_QUEUE, durable = "true"),
            exchange = @Exchange(name = TOPIC_MESSAGES_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = {TOPIC_MESSAGES_EXCHANGE_WITH_MESSAGES_LIKECOMMENT_QUEUE_ROUTING_KEY}
    ))
    public void listenLikeComment(LikeComment likeComment) {
        if (likeComment.getId() != null) {
            likeCommentService.removeLikeComment(likeComment.getId());
            return;
        }
        likeCommentService.addLikeComment(likeComment);
    }
}
