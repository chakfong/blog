package com.chakfong.blog.entity.status_enum;

import lombok.Getter;

@Getter
public enum UserFriendApplicationStatus implements IBaseStatusEnum{
    DEFAULT(0, "默认"),
    PROCESSING(1, "申请中"),
    PERMITTED(2, "已通过"),
    REJECTED(3, "已拒绝");

    private Integer value;

    private String display;

    UserFriendApplicationStatus(int value, String display) {
        this.value = value;
        this.display = display;
    }
}