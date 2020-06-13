package com.chakfong.blog.dto.request;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class PostCommentDto {
    @NotNull
    @Size(min = 1, max = 10)
    private Long dynamicId;

    @NotNull
    @Size(min = 1, max = 500)
    private String content;

    @NotNull
    private Integer layer;

    private Long lastLayerCommentId;

}
