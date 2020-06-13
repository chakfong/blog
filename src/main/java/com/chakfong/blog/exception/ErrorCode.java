package com.chakfong.blog.exception;

public enum ErrorCode {

    SUCCESS(200,20000),
    BAD_PARAM(200, 20001),
    CAPTCHA_EXPIRED(200,20002),
    CAPTCHA_UNCORRECTED(200,20003),
    NO_USER(200,20004),
    NO_DYNAMIC(200,20005),
    DYNAMIC_LIKED(200,20006),

    BAD_REQUEST(400, 40000),
    UNAUTHORIZED(401, 401001),
    NO_TOKEN(401, 40101),
    FORBIDDEN(403, 40300),

    INTERNAL_SERVER_ERROR(500, 50000);


    private int httpStatus;
    private int code;

    ErrorCode(int httpStatus, int code) {
        this.httpStatus = httpStatus;
        this.code = code;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public int getCode() {
        return code;
    }
}
