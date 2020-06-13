package com.chakfong.blog.dto.response;

import lombok.Data;

@Data
public class IsFriendDto {
    private Long userId;
    private boolean isFriend;

    public IsFriendDto(Long userId, boolean isFriend) {
        this.userId = userId;
        this.isFriend = isFriend;
    }
}