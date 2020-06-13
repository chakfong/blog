package com.chakfong.blog.configuration.properties;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(ignoreUnknownFields = false, prefix = "image.server")
public class ImageServerProperties {

    private String host;
    private String port;
    private String avatarPath;
    private String dynamicPath;

}
