package com.chakfong.blog.entity;

import lombok.Getter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "authority")
public class Authority {

    @Id
    @Column(name = "name", length = 50, nullable = false)
    private String name;

    public Authority() {
    }

    public Authority(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Authority authority = (Authority) o;
        return name.equals(authority.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "Authority{" +
                "name=" + name +
                '}';
    }

    public static final Authority ROLE_USER = new Authority(Role.USER.getName());

    public static final Authority ROLE_ADMIN = new Authority(Role.ADMIN.getName());

    @Getter
    public enum Role {
        USER(0, "ROLE_USER"),
        ADMIN(1, "ROLE_ADMIN");

        private Integer value;

        private String name;

        Role(int value, String name) {
            this.value = value;
            this.name = name;
        }
    }
}
