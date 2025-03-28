package com.example.messages.listener;

import com.example.littleredbook.entity.LikeNote;
import com.example.messages.service.ILikeNoteService;
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
public class LikeNoteListener {
    private final ILikeNoteService likeNoteService;
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MESSAGES_LIKENOTE_QUEUE, durable = "true"),
            exchange = @Exchange(name = TOPIC_MESSAGES_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = {TOPIC_MESSAGES_EXCHANGE_WITH_MESSAGES_LIKENOTE_QUEUE_ROUTING_KEY}
    ))
    public void listenLikeNote(LikeNote likeNote) {
        if (likeNote.getId() != null) {
            likeNoteService.removeLikeNote(likeNote.getId());
            return;
        }
        likeNoteService.addLikeNote(likeNote);
    }
}
