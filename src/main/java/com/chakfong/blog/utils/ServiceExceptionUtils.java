package com.chakfong.blog.utils;

import com.chakfong.blog.exception.ErrorCode;
import com.chakfong.blog.exception.ServiceException;
import com.google.common.base.Strings;

import java.util.Objects;


public class ServiceExceptionUtils {

    private ServiceExceptionUtils(){
        throw new IllegalStateException("不能实例化");
    }

    public static void throwIf(boolean condition, ErrorCode errorCode, String message) {
        if (condition)
            throw new ServiceException(message, errorCode);
    }

    public static void throwIfFalse(boolean condition, ErrorCode errorCode, String message) {
        throwIf(!condition, errorCode, message);
    }

    public static void throwIfNull(Object object, ErrorCode errorCode, String message) {
        if (Objects.isNull(object))
            throw new ServiceException(message, errorCode);
    }

    public static void throwIfNullOrEmpty(String object, ErrorCode errorCode, String message) {
        if (Strings.isNullOrEmpty(object))
            throw new ServiceException(message, errorCode);
    }

    public static void throwIfNotEquals(Object fromObject, Object toObject, ErrorCode errorCode, String message) {
        if (!Objects.equals(fromObject,toObject))
            throw new ServiceException(message, errorCode);
    }

}
