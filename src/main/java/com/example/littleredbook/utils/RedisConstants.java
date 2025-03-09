package com.example.littleredbook.utils;

public class RedisConstants {
    public static final String LOGIN_CODE_KEY = "little_red_book:login:code:";
    public static final Long LOGIN_CODE_TTL = 2L;
    public static final String LOGIN_USER_KEY = "little_red_book:login:token:";
    public static final Long LOGIN_USER_TTL = 30L;
    public static final String CACHE_USER_KEY = "little_red_book:cache:user:";
    public static final Long CACHE_USER_TTL = 100L;
    public static final Long CACHE_NULL_TTL = 2L;
    public static final String LOCK_PREFIX = "little_red_book:lock:string:";
    public static final String LOCK_HASH_PREFIX = "little_red_book:lock:hash:";
    public static final String USER_NAME = "user_";
    public static final String CACHE_TAG_KEY = "little_red_book:cache:tag:";
    public static final String CACHE_NOTE_KEY = "little_red_book:cache:note:";
    public static final String CACHE_TAGLIST_KEY = "little_red_book:cache:tag:list";
    public static final String CACHE_NOTELIST_KEY = "little_red_book:cache:note:list";
    public static final Long CACHE_TAGLIST_TTL = 30L;
    public static final Long CACHE_NOTELIST_TTL = 30L;
    public static final Long CACHE_NOTE_TTL = 10L;
    public static final String CACHE_NOTE_USER_KEY = "little_red_book:cache:note:user:";
    public static final Long CACHE_NOTE_USER_TTL = 10L;
    public static final String CACHE_BROWSE_KEY = "little_red_book:cache:browse:";
    public static final Long CACHE_BROWSE_TTL = 10L;
    public static final String CACHE_BROWSE_USER_KEY = "little_red_book:cache:browse:user:";
    public static final Long CACHE_BROWSE_USER_TTL = 30L;
    public static final String CACHE_COLLECTIONS_KEY = "little_red_book:cache:collections:";
    public static final Long CACHE_COLLECTIONS_TTL = 10L;
    public static final String CACHE_COLLECTIONS_USER_KEY = "little_red_book:cache:collections:user:";
    public static final Long CACHE_COLLECTIONS_USER_TTL = 30L;
    public static final String CACHE_CONCERN_KEY = "little_red_book:cache:collections:";
    public static final Long CACHE_CONCERN_TTL = 10L;
    public static final String CACHE_CONCERN_USER_KEY = "little_red_book:cache:collections:user:";
    public static final Long CACHE_CONCERN_USER_TTL = 30L;
    public static final String CACHE_LIKECOMMENT_KEY = "little_red_book:cache:like_comment:";
    public static final Long CACHE_LIKECOMMENT_TTL = 10L;
    public static final String CACHE_LIKECOMMENT_COMMENT_KEY = "little_red_book:cache:like_comment:comment:";
    public static final Long CACHE_LIKECOMMENT_COMMENT_TTL = 30L;
    public static final String CACHE_LIKENOTE_KEY = "little_red_book:cache:like_note:";
    public static final Long CACHE_LIKENOTE_TTL = 10L;
    public static final String CACHE_LIKENOTE_NOTE_KEY = "little_red_book:cache:like_note:note:";
    public static final Long CACHE_LIKENOTE_NOTE_TTL = 30L;
    public static final String CACHE_LIKEREPLY_KEY = "little_red_book:cache:like_reply:";
    public static final Long CACHE_LIKEREPLY_TTL = 10L;
    public static final String CACHE_LIKEREPLY_REPLY_KEY = "little_red_book:cache:like_reply:note:";
    public static final Long CACHE_LIKEREPLY_REPLY_TTL = 30L;
    public static final String CACHE_MESSAGE_KEY = "little_red_book:cache:message:";
    public static final Long CACHE_MESSAGE_TTL = 10L;
    public static final String CACHE_MESSAGE_SENDERANDRECEIVER_KEY = "little_red_book:cache:message:sender_and_receiver:";
    public static final Long CACHE_MESSAGE_SENDERANDRECEIVER_TTL = 30L;
    public static final Long CACHE_TAG_TTL = 60L;
    public static final Long CACHE_SHOP_TTL = 30L;
    public static final String CACHE_SHOP_KEY = "cache:shop:";

    public static final String CACHE_SHOPTYPE_KEY = "cache:shopType";

    public static final String LOCK_SHOP_KEY = "lock:shop:";
    public static final Long LOCK_SHOP_TTL = 10L;

    public static final String SECKILL_STOCK_KEY = "seckill:stock:";
    public static final String BLOG_LIKED_KEY = "blog:liked:";
    public static final String FEED_KEY = "feed:";
    public static final String SHOP_GEO_KEY = "shop:geo:";
    public static final String USER_SIGN_KEY = "sign:";
}
