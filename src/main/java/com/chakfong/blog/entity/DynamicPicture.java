package com.chakfong.blog.entity;

import lombok.Data;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
@Table(name = "dynamic_picture")
public class DynamicPicture {

    @Id
    @Column(name = "dynamic_picture_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dynamicPictureId;

    @Column(name = "picture", nullable = false)
    private String picture;

    @ManyToOne(optional = false, cascade = CascadeType.DETACH)
    @JoinColumn(name = "dynamic_id")
    private Dynamic dynamic;

    public DynamicPicture() {
    }

    public DynamicPicture(String picture) {
        this.picture = picture;
    }
}
