package com.chakfong.blog.entity;

import com.chakfong.blog.dto.response.DynamicDto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.hibernate.annotations.BatchSize;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Data
@Table(name = "dynamic")
public class Dynamic extends BaseEntity {

    @Id
    @Column(name = "dynamic_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dynamicId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id") //该表中的关联字段
    private User user;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "visible", nullable = false)
    private Integer visible;

    @Column(name = "like_count", nullable = false)
    private Integer likeCount;

    @OneToMany(mappedBy = "dynamic", fetch = FetchType.LAZY)
//    @OneToMany(fetch = FetchType.LAZY)
//    @JoinColumn(name = "dynamic_id")
    @JsonIgnoreProperties(value = {"dynamic"})
    private List<DynamicPicture> pictureList;

    @OneToMany(mappedBy = "dynamic", fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = {"dynamic"})
    private List<DynamicComment> commentList;

    public DynamicDto toDynamicDto() {
        return new DynamicDto(dynamicId, user, title, visible, likeCount, this.getCreateTime());
    }
}
