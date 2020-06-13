package com.chakfong.blog.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
@Table(name = "dynamic_like")
public class DynamicLike extends BaseEntity{

    @Id
    @Column(name = "dynamic_like_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dynamicLikeId;

    @Column(name = "dynamic_id", nullable = false)
    private Long dynamicId;

    @Column(name = "user_id", nullable = false)
    private Long userId;


}
