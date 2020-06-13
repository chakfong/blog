package com.chakfong.blog.dto.response;


import com.chakfong.blog.exception.ErrorCode;

import java.io.Serializable;


public class Result<T> implements Serializable {

    private int code;

    private T data;

    private String message;

    public boolean success() {
        return this.code == ErrorCode.SUCCESS.getCode();
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "Result{" +
                "code=" + code +
                ", data=" + data +
                ", message='" + message + '\'' +
                '}';
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
