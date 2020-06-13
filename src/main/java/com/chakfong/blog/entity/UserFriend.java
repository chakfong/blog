package com.chakfong.blog.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.hibernate.annotations.BatchSize;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Data
@Table(name = "user_friend")
public class UserFriend extends BaseEntity{

    @Id
    @Column(name = "user_friend_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userFriendId;

    @Column(name = "from_user_id", nullable = false)
    private Long fromUserId;

    @JsonIgnore
    @Column(name = "to_user_id", nullable = false)
    private Long toUserId;


}
