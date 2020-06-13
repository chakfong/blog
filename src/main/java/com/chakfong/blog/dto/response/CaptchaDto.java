package com.chakfong.blog.dto.response;

import lombok.Data;

@Data
public class CaptchaDto {

    private boolean required;

    private String captcha;

}
