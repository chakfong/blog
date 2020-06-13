package com.chakfong.blog.entity;

import com.chakfong.blog.dto.response.UserDto;

public interface BaseUser {
    UserDto toDto();

    User toUser();
}
