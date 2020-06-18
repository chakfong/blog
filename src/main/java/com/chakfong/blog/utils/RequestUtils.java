package com.chakfong.blog.utils;

import javax.servlet.http.HttpServletRequest;

public class RequestUtils {

    private static final String UNKNOWN = "unknown";

    private RequestUtils() {
        throw new IllegalStateException("不能实例化");
    }

    public static String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (isNullOrBlankOrUnknown(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (isNullOrBlankOrUnknown(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (isNullOrBlankOrUnknown(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个路由时，取第一个非unknown的ip
        final String[] arr = ip.split(",");
        for (final String str : arr) {
            if (!UNKNOWN.equalsIgnoreCase(str)) {
                ip = str;
                break;
            }
        }
        return ip;
    }

    private static boolean isNullOrBlankOrUnknown(String string) {
        return StringUtils.isNullOrBlank(string) || UNKNOWN.equalsIgnoreCase(string);
    }
}
