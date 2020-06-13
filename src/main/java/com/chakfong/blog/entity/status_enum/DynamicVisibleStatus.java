package com.chakfong.blog.entity.status_enum;

import lombok.Getter;

@Getter
public enum DynamicVisibleStatus implements IBaseStatusEnum{
    DELETED(-1, "删除"),
    DEFAULT(0, "默认"),
    PUBLIC(1, "公开"),
    PRIVATE(2, "尽我可见"),
    FRIEND(3, "好友可见"),
    WHITE(4, "白名单可见");

    private Integer value;

    private String display;

    DynamicVisibleStatus(int value, String display) {
        this.value = value;
        this.display = display;
    }
}