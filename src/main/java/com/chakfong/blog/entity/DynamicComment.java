package com.chakfong.blog.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Data
@Table(name = "dynamic_comment")
public class DynamicComment extends BaseEntity {

    @Id
    @Column(name = "dynamic_comment_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dynamicCommentId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "layer", nullable = false)
    private Integer layer;

    @ManyToOne(optional = false,fetch = FetchType.LAZY)
    @JoinColumn(name = "last_layer_comment_id")
    private DynamicComment lastDynamicComment;

    @ManyToOne(optional = false)
    @JoinColumn(name = "dynamic_id")
    private Dynamic dynamic;

}
