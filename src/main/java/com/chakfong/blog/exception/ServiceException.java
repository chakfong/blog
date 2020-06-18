package com.chakfong.blog.exception;

public class ServiceException extends RuntimeException {

    private final ErrorCode code;


    public ServiceException(String message, ErrorCode code) {
        super(message);
        this.code = code;
    }

    public ErrorCode getCode() {
        return code;
    }

}
