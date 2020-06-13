package com.chakfong.blog.configuration.properties;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix="email")
public class EmailProperties {
    private String protocol;
    private String auth;
    private String host;
    private String port;
    private String username;
    private String password;
    private String ssl;
    private Boolean debug;
}
