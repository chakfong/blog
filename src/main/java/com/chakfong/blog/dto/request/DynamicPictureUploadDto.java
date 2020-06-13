package com.chakfong.blog.dto.request;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.File;

@Data
public class DynamicPictureUploadDto {
    @NotNull
    @Size(min = 1, max = 20)
    private Long dynamicId;

    @NotNull
    private File picture;
}
