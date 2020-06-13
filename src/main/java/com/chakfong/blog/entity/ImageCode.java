package com.chakfong.blog.entity;

import lombok.Data;

import java.awt.image.BufferedImage;
import java.time.LocalDateTime;

@Data
public class ImageCode {

    private BufferedImage image;

    private String code;

    private Integer expireTime;

    private String imageBase64;

    public ImageCode(BufferedImage image, String code, Integer expireIn, String imageBase64) {
        this.image = image;
        this.code = code;
        this.expireTime = expireIn;
        this.imageBase64 = imageBase64;
    }
}
