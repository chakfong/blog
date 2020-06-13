package com.chakfong.blog.security;

import org.springframework.util.Assert;

public class RequestIPHolder {

    private static final ThreadLocal<String> contextHolder = new ThreadLocal();

    public void clearContext() {
        contextHolder.remove();
    }

    public static String getContext() {
        String ctx = (String)contextHolder.get();
        return ctx;
    }

    public static void setContext(String context) {
        Assert.notNull(context, "获取不到客户端IP");
        contextHolder.set(context);
    }


}
