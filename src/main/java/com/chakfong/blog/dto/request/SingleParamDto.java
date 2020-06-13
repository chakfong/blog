package com.chakfong.blog.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class SingleParamDto<T> {

    @JsonAlias(value = {"email","userId","dynamicId"})
    private T param;

    public T get(){
        return this.param;
    }
}
