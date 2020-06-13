package com.chakfong.blog.entity.status_enum;


import java.util.Objects;

public interface IBaseStatusEnum {
    String getDisplay();

    Integer getValue();

    static <T extends IBaseStatusEnum> T fromValue(Class<T> enumType, Integer value) {
        for (T object : enumType.getEnumConstants()) {
            if (Objects.equals(value, object.getValue())) {
                return object;
            }
        }
        throw new IllegalArgumentException("No enum value " + value + " of " + enumType.getCanonicalName());
    }
}
