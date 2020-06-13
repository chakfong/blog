package com.chakfong.blog.utils;

public class StringUtils {
    public static boolean isNullOrBlank(String string) {
        return string == null || string.trim().equals("");
    }

    public static boolean isNotNullOrBlank(String string){
        return string != null && !string.trim().equals("");
    }
}
