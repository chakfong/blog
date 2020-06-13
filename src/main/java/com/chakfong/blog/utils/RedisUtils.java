package com.chakfong.blog.utils;

import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;

public class RedisUtils {

    private RedisUtils() {

    }

    private static final String PREFIX = "BLOG";

    public static String keySplice(Object... keys) {
        StringBuilder keyString = new StringBuilder();
        keyString.append(PREFIX);
        for (Object key : keys) {
            keyString.append(":")
                    .append(key);
        }
        return keyString.toString();
    }


    public static <T> T getCache(RedissonClient redissonClient, Object... keys) {
        if (keys != null) {
            String key = RedisUtils.keySplice(keys);
            RBucket<T> objectRBucket = redissonClient.getBucket(key);
            return objectRBucket.get();
        }
        return null;
    }

    public static <T> void setCache(RedissonClient redissonClient, T object, Object... keys) {
        String key = RedisUtils.keySplice(keys);
        RBucket<T> objectRBucket = redissonClient.getBucket(key);
        objectRBucket.set(object);
    }

    public static <T> boolean removeCache(RedissonClient redissonClient, Object... keys) {
        String key = RedisUtils.keySplice(keys);
        RBucket<T> objectRBucket = redissonClient.getBucket(key);
        return objectRBucket.delete();
    }
}
