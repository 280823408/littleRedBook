package com.example.littleredbook.utils;

public class RedisConstants {
    public static final String LOGIN_CODE_KEY = "littleredbook:login:code:";
    public static final Long LOGIN_CODE_TTL = 2L;
    public static final String LOGIN_USER_KEY = "littleredbook:login:token:";
    public static final Long LOGIN_USER_TTL = 30L;
    public static final Long CACHE_NULL_TTL = 2L;
    public static final String LOCK_PREFIX = "littleredbook:lock:string:";
    public static final String LOCK_HASH_PREFIX = "littleredbook:lock:hash:";
    public static final String USER_NAME = "user_";
    public static final String CACHE_TAG_KEY = "littleredbook:cache:tag";
    public static final String CACHE_NOTE_KEY = "littleredbook:cache:note";
    public static final Long CACHE_NOTE_TTL = 10L;
    public static final String CACHE_NOTE_USER_KEY = "littleredbook:cache:note:user";
    public static final Long CACHE_NOTE_USER_TTL = 10L;
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
