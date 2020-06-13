package com.chakfong.blog.dto.request;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class KeySearchDto {
    @NotNull
    @Size(min = 1, max = 50)
    private String searchType;

    @NotNull
    @Size(min = 2, max = 100)
    private String key;

}
