package com.chakfong.blog.dto.request;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class PostDynamicDto {
    @NotNull
    @Size(min = 4, max = 50)
    private String title;
    @NotNull
    @Size(min = 10)
    private String content;
    @NotNull
    @Size(min = 1, max = 1)
    private Integer visible;
}
