package com.chakfong.blog.dto.request;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class RegisterDto {
    @NotNull
    @Size(min = 1, max = 50)
    private String username;

    @NotNull
    @Size(min = 4, max = 100)
    private String password;

    @NotNull
    @Email
    @Size(min = 4, max = 100)
    private String email;

    @NotNull
    @Size(min = 6, max = 6)
    private String code;
}
