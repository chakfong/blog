package com.chakfong.blog.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
@Table(name = "user_friend_application")
public class UserFriendApplication extends BaseEntity{

    @Id
    @Column(name = "ufa_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ufaId;

    @Column(name = "from_user_id", nullable = false)
    private Long fromUserId;

    @JsonIgnore
    @Column(name = "to_user_id", nullable = false)
    private Long toUserId;

    @Column(name = "status", nullable = false)
    private Integer status;

}
