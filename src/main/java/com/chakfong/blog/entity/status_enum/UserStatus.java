package com.chakfong.blog.entity.status_enum;

import lombok.Getter;

@Getter
public enum UserStatus implements IBaseStatusEnum{
    DELETED(-1, "删除"),
    INACTIVE(0, "禁用"),
    ACTIVE(1, "启用");

    private Integer value;

    private String display;

    UserStatus(int value, String display) {
        this.value = value;
        this.display = display;
    }
}