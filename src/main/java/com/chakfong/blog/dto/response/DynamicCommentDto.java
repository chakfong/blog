package com.chakfong.blog.dto.response;

import com.chakfong.blog.entity.DynamicComment;
import com.chakfong.blog.entity.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DynamicCommentDto {
    private Long dynamicCommentId;

    private User user;

    private String content;

    private List<DynamicCommentDto> nextLayerDynamicComments;

    private Date createTime;

    public DynamicCommentDto() {
    }

    public DynamicCommentDto(Long dynamicCommentId, String content,User user, Date createTime) {
        this.dynamicCommentId = dynamicCommentId;
        this.content = content;
        this.user = user;
        this.createTime = createTime;
    }

    public static DynamicCommentDto parseDynamicComment(DynamicComment dynamicComment) {
        return new DynamicCommentDto(dynamicComment.getDynamicCommentId(),
                dynamicComment.getContent(),
                dynamicComment.getUser(),
                dynamicComment.getCreateTime());
    }
}
