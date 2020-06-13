package com.chakfong.blog.dto.response;

import com.chakfong.blog.entity.Authority;
import com.chakfong.blog.entity.BaseUser;
import com.chakfong.blog.entity.User;
import lombok.Data;
import org.hibernate.collection.internal.PersistentSet;

import java.util.*;

@Data
public class UserDto implements BaseUser {

    private Long userId;

    private String username;

    private String email;

    private String avatar;

    private Integer status;

    private Set<Authority> authorities = Collections.emptySet();

    private List<User> blacklist = Collections.emptyList();

    private List<User> whitelist = Collections.emptyList();

    public UserDto(){}

    public UserDto(Long userId, String username, String email, String avatar, Integer status, Set<Authority> authorities, List<User> blacklist, List<User> whitelist) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.avatar = avatar;
        this.status = status;
        this.authorities = authorities;
        this.blacklist = blacklist;
        this.whitelist = whitelist;
    }

    public User toUser() {
        return new User(userId, username, email, avatar, status, authorities, blacklist, whitelist);
    }

    public UserDto toDto(){
        return this;
    }
}
