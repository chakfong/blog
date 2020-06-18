package com.chakfong.blog.utils;

public class StringUtils {

    private StringUtils(){
        throw new IllegalStateException("不能实例化");
    }
    
    public static boolean isNullOrBlank(String string) {
        return string == null || string.trim().equals("");
    }

    public static boolean isNotNullOrBlank(String string){
        return string != null && !string.trim().equals("");
    }
}
