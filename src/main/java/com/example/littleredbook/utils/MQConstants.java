package com.example.littleredbook.utils;

public interface MQConstants {
    /** 主要解决远程调用以及缓存更新中可异步的操作 */
    String DIRECT_ERROR_EXCHANGE = "error.direct";
    String TOPIC_MESSAGES_EXCHANGE = "messages.topic";
    String TOPIC_NOTES_EXCHANGE = "notes.topic";
    String TOPIC_USER_EXCHANGE = "user.topic";
    String TOPIC_SEARCH_EXCHANGE = "search.topic";
    String TOPIC_COMMUNITY_EXCHANGE = "community.topic";
    String ERROR_QUEUE = "error.queue";
    String DIRECT_ERROR_EXCHANGE_WITH_ERROR_QUEUE_ROUTING_KEY = "error";
    /** 消息模块 */
    String MESSAGES_LIKECOMMENT_LIKE_QUEUE = "likeComment.like.queue";
    String TOPIC_MESSAGES_EXCHANGE_WITH_MESSAGES_LIKECOMMENT_LIKE_QUEUE_ROUTING_KEY = "messages.likecomment.like";
    String MESSAGES_LIKECOMMENT_CACHE_ADD_QUEUE = "likeComment.cache.add.queue";
    String TOPIC_MESSAGES_EXCHANGE_WITH_MESSAGES_LIKECOMMENT_CACHE_ADD_QUEUE_ROUTING_KEY = "messages.likecomment.cache.add";
    String MESSAGES_LIKECOMMENT_CACHE_DELETE_QUEUE = "likeComment.cache.delete.queue";
    String TOPIC_MESSAGES_EXCHANGE_WITH_MESSAGES_LIKECOMMENT_CACHE_DELETE_QUEUE_ROUTING_KEY = "messages.likecomment.cache.delete";
    String MESSAGES_LIKENOTE_LIKE_QUEUE = "likeNote.like.queue";
    String TOPIC_MESSAGES_EXCHANGE_WITH_MESSAGES_LIKENOTE_LIKE_QUEUE_ROUTING_KEY = "messages.likenote.like";
    String MESSAGES_LIKENOTE_CACHE_ADD_QUEUE = "likeNote.cache.add.queue";
    String TOPIC_MESSAGES_EXCHANGE_WITH_MESSAGES_LIKENOTE_CACHE_ADD_QUEUE_ROUTING_KEY = "messages.likenote.cache.add";
    String MESSAGES_LIKENOTE_CACHE_DELETE_QUEUE = "likeNote.cache.delete.queue";
    String TOPIC_MESSAGES_EXCHANGE_WITH_MESSAGES_LIKENOTE_CACHE_DELETE_QUEUE_ROUTING_KEY = "messages.likenote.cache.delete";
    String MESSAGES_LIKEREPLY_LIKE_QUEUE = "likeReply.like.queue";
    String TOPIC_MESSAGES_EXCHANGE_WITH_MESSAGES_LIKEREPLY_LIKE_QUEUE_ROUTING_KEY = "messages.likereply.like";
    String MESSAGES_LIKEREPLY_CACHE_ADD_QUEUE = "likeReply.cache.add.queue";
    String TOPIC_MESSAGES_EXCHANGE_WITH_MESSAGES_LIKEREPLY_CACHE_ADD_QUEUE_ROUTING_KEY = "messages.likereply.cache.add";
    String MESSAGES_LIKEREPLY_CACHE_DELETE_QUEUE = "likeReply.cache.delete.queue";
    String TOPIC_MESSAGES_EXCHANGE_WITH_MESSAGES_LIKEREPLY_CACHE_DELETE_QUEUE_ROUTING_KEY = "messages.likereply.cache.delete";
    String MESSAGES_MESSAGE_CACHE_ADD_QUEUE = "message.cache.add.queue";
    String TOPIC_MESSAGES_EXCHANGE_WITH_MESSAGES_MESSAGE_CACHE_ADD_QUEUE_ROUTING_KEY = "messages.message.cache.add";
    String MESSAGES_MESSAGE_CACHE_DELETE_QUEUE = "message.cache.delete.queue";
    String TOPIC_MESSAGES_EXCHANGE_WITH_MESSAGES_MESSAGE_CACHE_DELETE_QUEUE_ROUTING_KEY = "messages.message.cache.delete";
    /** 笔记模块 */
    String NOTES_COMMENT_CACHE_LIKE_QUEUE = "comment.cache.like.queue";
    String TOPIC_NOTES_EXCHANGE_WITH_NOTES_COMMENT_CACHE_LIKE_QUEUE_ROUTING_KEY = "notes.comment.cache.like";
    String NOTES_COMMENT_CACHE_ADD_QUEUE = "comment.cache.add.queue";
    String TOPIC_NOTES_EXCHANGE_WITH_NOTES_COMMENT_CACHE_ADD_QUEUE_ROUTING_KEY = "notes.comment.cache.add";
    String NOTES_COMMENT_CACHE_DELETE_QUEUE = "comment.cache.delete.queue";
    String TOPIC_NOTES_EXCHANGE_WITH_NOTES_COMMENT_CACHE_DELETE_QUEUE_ROUTING_KEY = "notes.comment.cache.delete";
    String NOTES_NOTE_CACHE_LIKE_QUEUE = "note.cache.like.queue";
    String TOPIC_NOTES_EXCHANGE_WITH_NOTES_NOTE_CACHE_LIKE_QUEUE_ROUTING_KEY = "notes.note.cache.like";
    String NOTES_NOTE_CACHE_COLLECTION_QUEUE = "note.cache.collection.queue";
    String TOPIC_NOTES_EXCHANGE_WITH_NOTES_NOTE_CACHE_COLLECTION_QUEUE_ROUTING_KEY = "notes.note.cache.collection";
    String NOTES_NOTE_CACHE_ADD_QUEUE = "note.cache.add.queue";
    String TOPIC_NOTES_EXCHANGE_WITH_NOTES_NOTE_CACHE_ADD_QUEUE_ROUTING_KEY = "notes.note.cache.add";
    String NOTES_NOTE_CACHE_DELETE_QUEUE = "note.cache.delete.queue";
    String TOPIC_NOTES_EXCHANGE_WITH_NOTES_NOTE_CACHE_DELETE_QUEUE_ROUTING_KEY = "notes.note.cache.delete";
    String NOTES_REPLY_CACHE_LIKE_QUEUE = "reply.cache.like.queue";
    String TOPIC_NOTES_EXCHANGE_WITH_NOTES_REPLY_CACHE_LIKE_QUEUE_ROUTING_KEY = "notes.reply.cache.like";
    String NOTES_REPLY_CACHE_ADD_QUEUE = "reply.cache.add.queue";
    String TOPIC_NOTES_EXCHANGE_WITH_NOTES_REPLY_CACHE_ADD_QUEUE_ROUTING_KEY = "notes.reply.cache.add";
    String NOTES_REPLY_CACHE_DELETE_QUEUE = "reply.cache.delete.queue";
    String TOPIC_NOTES_EXCHANGE_WITH_NOTES_REPLY_CACHE_DELETE_QUEUE_ROUTING_KEY = "notes.reply.cache.delete";
    /** 用户模块 */
    String USERCENTER_USER_CACHE_LIKE_QUEUE = "user.cache.like.queue";
    String TOPIC_USER_EXCHANGE_WITH_USERCENTER_USER_CACHE_LIKE_QUEUE_ROUTING_KEY = "usercenter.user.cache.like";
    String USERCENTER_USER_CACHE_ADD_QUEUE = "user.cache.add.queue";
    String TOPIC_USER_EXCHANGE_WITH_USERCENTER_USER_CACHE_ADD_QUEUE_ROUTING_KEY = "usercenter.user.cache.add";
    String USERCENTER_USER_CACHE_DELETE_QUEUE = "user.cache.delete.queue";
    String TOPIC_USER_EXCHANGE_WITH_USERCNETER_USER_CACHE_DELETE_QUEUE_ROUTING_KEY = "usercenter.user.cache.delete";
    String USERCENTER_BROWSERECORD_CACHE_ADD_QUEUE = "browserecord.cache.add.queue";
    String TOPIC_USER_EXCHANGE_WITH_USERCENTER_BROWSERECORD_CACHE_ADD_QUEUE_ROUTING_KEY = "usercenter.browserecord.cache.add";
    String USERCNETER_BROWSERECORD_CACHE_DELETE_QUEUE = "browserecord.cache.delete.queue";
    String TOPIC_USER_EXCHANGE_WITH_USERCENTER_BROWSERECORD_CACHE_DELETE_QUEUE_ROUTING_KEY = "usercenter.browserecord.cache.delete";
    String USERCENTER_COLLECTIONS_CACHE_LIKE_QUEUE = "collections.cache.like.queue";
    String TOPIC_USER_EXCHANGE_WITH_USERCENTER_COLLECTIONS_CACHE_LIKE_QUEUE_ROUTING_KEY = "usercenter.collections.cache.like";
    String USERCENTER_COLLECTIONS_CACHE_ADD_QUEUE = "collections.cache.add.queue";
    String TOPIC_USER_EXCHANGE_WITH_USERCENTER_COLLECTIONS_CACHE_ADD_QUEUE_ROUTING_KEY = "usercenter.collections.cache.add";
    String USERCENTER_COLLECTIONS_CACHE_DELETE_QUEUE = "collections.cache.delete.queue";
    String TOPIC_USER_EXCHANGE_WITH_USERCENTER_COLLECTIONS_CACHE_DELETE_QUEUE_ROUTING_KEY = "usercenter.collections.cache.delete";
    /** 搜索模块 */
    String SEARCH_SEARCHRECORD_CACHE_ADD_QUEUE = "searchrecord.cache.add.queue";
    String TOPIC_SEARCH_EXCHANGE_WITH_SEARCH_SEARCHRECORD_CACHE_ADD_QUEUE_ROUTING_KEY = "search.searchrecord.cache.add";
    String SEARCH_SEARCHRECORD_CACHE_DELETE_QUEUE = "searchrecord.cache.delete.queue";
    String TOPIC_SEARCH_EXCHANGE_WITH_SEARCH_SEARCHRECORD_CACHE_DELETE_QUEUE_ROUTING_KEY = "search.searchrecord.cache.delete";
    /** 社区模块 */
    String COMMUNITY_CONRERN_CACHE_ADD_QUEUE = "conrern.cache.add.queue";
    String TOPIC_COMMUNITY_EXCHANGE_WITH_COMMUNITY_CONRERN_CACHE_ADD_QUEUE_ROUTING_KEY = "community.conrern.cache.add";
    String COMMUNITY_CONRERN_CACHE_DELETE_QUEUE = "conrern.cache.delete.queue";
    String TOPIC_COMMUNITY_EXCHANGE_WITH_COMMUNITY_CONRERN_CACHE_DELETE_QUEUE_ROUTING_KEY = "community.conrern.cache.delete";
    String COMMUNITY_CONCERN_LIKE_QUEUE = "concern.like.queue";
    String TOPIC_COMMUNITY_EXCHANGE_WITH_COMMUNITY_CONCERN_LIKE_QUEUE_ROUTING_KEY = "community.concern.like";
}
