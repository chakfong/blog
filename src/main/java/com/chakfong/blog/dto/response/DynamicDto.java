package com.chakfong.blog.dto.response;

import com.chakfong.blog.entity.User;
import lombok.Data;

import java.util.Date;

@Data
public class DynamicDto {

    private Long dynamicId;

    private User user;

    private String title;

    private Integer visible;

    private Integer likeCount;

    private Date createTime;

    public DynamicDto(Long dynamicId, User user, String title, Integer visible, Integer likeCount, Date createTime) {
        this.dynamicId = dynamicId;
        this.user = user;
        this.title = title;
        this.visible = visible;
        this.likeCount = likeCount;
        this.createTime = createTime;
    }
}
