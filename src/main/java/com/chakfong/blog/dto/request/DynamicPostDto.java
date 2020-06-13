package com.chakfong.blog.dto.request;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class DynamicPostDto {
    @NotNull
    @Size(min = 2, max = 50)
    private String title;

    @NotNull
    @Size(min = 10)
    private String content;

    @NotNull
    private Integer visible;
}
