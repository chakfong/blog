package com.chakfong.blog.utils;

import com.chakfong.blog.exception.CheckException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import java.util.List;

public class CheckUtils {

    public static void notNull(Object obj, String message) {
        if (obj == null) {
            fail(message);
        }
    }

    public static void notEquals(Object toObj, Object fromObj, String message) {
        if (!toObj.equals(fromObj)) {
            fail(message);
        }
    }

    public static void inList(Object toObj, List<Object> fromObj, String message) {

        if (!fromObj.contains(toObj)) {
            fail(message);
        }
    }

    private static void fail(String message) {
        throw new CheckException(message);
    }
}
