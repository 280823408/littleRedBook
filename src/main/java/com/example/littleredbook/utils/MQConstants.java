package com.example.littleredbook.utils;

public interface MQConstants {
    /** 主要解决远程调用慢以及修改缓存中可异步的操作 */
    String DIRECT_ERROR_EXCHANGE = "error.direct";
    String TOPIC_MESSAGES_EXCHANGE = "messages.topic";
    String TOPIC_NOTES_EXCHANGE = "notes.topic";
    String ERROR_QUEUE = "error.queue";
    String DIRECT_ERROR_EXCHANGE_WITH_ERROR_QUEUE_ROUTING_KEY = "error";
    /** 消息模块 */
    String MESSAGES_LIKECOMMENT_QUEUE = "likeComment.queue";
    String TOPIC_MESSAGES_EXCHANGE_WITH_MESSAGES_LIKECOMMENT_QUEUE_ROUTING_KEY = "messages.likecomment.like";
    String MESSAGES_LIKENOTE_QUEUE = "likeNote.queue";
    String TOPIC_MESSAGES_EXCHANGE_WITH_MESSAGES_LIKENOTE_QUEUE_ROUTING_KEY = "messages.likenote.like";
    String MESSAGES_LIKEREPLY_QUEUE = "likeReply.queue";
    String TOPIC_MESSAGES_EXCHANGE_WITH_MESSAGES_LIKEREPLY_QUEUE_ROUTING_KEY = "messages.likereply.like";
    /** 笔记模块 */
    String NOTES_COMMENT_CACHE_QUEUE = "comment.cache.queue";
    String TOPIC_NOTES_EXCHANGE_WITH_NOTES_COMMENT_CACHE_QUEUE_ROUTING_KEY = "notes.comment.cache.like";
    String NOTES_NOTE_CACHE_QUEUE = "note.cache.queue";
    String TOPIC_NOTES_EXCHANGE_WITH_NOTES_NOTE_CACHE_QUEUE_ROUTING_KEY = "notes.note.cache.like";
    String NOTES_REPLY_CACHE_QUEUE = "reply.cache.queue";
    String TOPIC_NOTES_EXCHANGE_WITH_NOTES_REPLY_CACHE_QUEUE_ROUTING_KEY = "notes.reply.cache.like";
}
