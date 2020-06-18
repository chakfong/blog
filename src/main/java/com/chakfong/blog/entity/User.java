package com.chakfong.blog.entity;

import com.chakfong.blog.dto.response.UserDto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.BatchSize;

import javax.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Entity
@Data
@Table(name = "user")
@Slf4j
//@JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler", "fieldHandler"})
public class User extends BaseEntity implements Cloneable, BaseUser {

    public User() {
    }

    public User(Long userId, String username, String email, String avatar, Integer status, Set<Authority> authorities, List<User> blacklist, List<User> whitelist) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.avatar = avatar;
        this.status = status;
        this.authorities = authorities;
        this.blacklist = blacklist;
        this.whitelist = whitelist;
    }

    @Id
    @Column(name = "user_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(name = "username", length = 50, unique = true, nullable = false)
    private String username;

    @JsonIgnore
    @Column(name = "password", length = 100, nullable = false)
    private String password;

    @Column(name = "email", length = 50, unique = true, nullable = false)
    private String email;

    @Column(name = "avatar", length = 100, unique = true)
    private String avatar;

    @Column(name = "status")
    private Integer status;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_authority",
            joinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "user_id")},
            inverseJoinColumns = {@JoinColumn(name = "name", referencedColumnName = "name")})
    @BatchSize(size = 20)
    @JsonIgnore
    private Set<Authority> authorities = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_blacklist",
            joinColumns = {@JoinColumn(name = "from_user_id", referencedColumnName = "user_id")},
            inverseJoinColumns = {@JoinColumn(name = "to_user_id", referencedColumnName = "user_id")}
    )
    @JsonIgnore
    private List<User> blacklist;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_whitelist",
            joinColumns = {@JoinColumn(name = "from_user_id", referencedColumnName = "user_id")},
            inverseJoinColumns = {@JoinColumn(name = "to_user_id", referencedColumnName = "user_id")}
    )
    @JsonIgnore
    private List<User> whitelist;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY) // 多端的实体类字段名
    @JsonIgnore
    private List<Dynamic> dynamics;


    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<DynamicComment> dynamicsComment;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return userId.equals(user.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    @Override
    public User clone() {
        User user = null;
        try {
            user = (User) super.clone();
        } catch (CloneNotSupportedException e) {
            log.warn(e.getMessage());
        }
        return user;
    }

    public UserDto toDto() {
        return new UserDto(userId, username, email, avatar, status, authorities, blacklist, whitelist);
    }

    public User toUser() {
        return this;
    }
}
