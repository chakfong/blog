package com.chakfong.blog.web;

import com.chakfong.blog.dto.request.PostCommentDto;
import com.chakfong.blog.dto.request.PostDynamicDto;
import com.chakfong.blog.dto.response.DynamicCommentDto;
import com.chakfong.blog.dto.response.DynamicDto;
import com.chakfong.blog.dto.response.Result;
import com.chakfong.blog.dto.response.ResultBuilder;
import com.chakfong.blog.entity.Dynamic;
import com.chakfong.blog.service.DynamicService;
import com.chakfong.blog.utils.CheckUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
@Slf4j
public class DynamicController {


    private final DynamicService dynamicService;

    public DynamicController(DynamicService dynamicService) {
        this.dynamicService = dynamicService;
    }

    /**
     * 获取公开动态列表
     *
     * @return
     */
    @GetMapping("/dynamic/public/all")
    public Result<List<DynamicDto>> getPublicDynamic() {
        return ResultBuilder.onSuc(dynamicService.getPublicDynamic());
    }

    /**
     * 获取个人动态列表
     *
     * @return
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/dynamic/my")
    public Result<List<DynamicDto>> getMyDynamic() {
        return ResultBuilder.onSuc(dynamicService.getDynamicWithAuthorities());
    }

    /**
     * 获取用户动态列表
     *
     * @param userId
     * @return
     */
    @GetMapping("/dynamic/user/{userId}")
    public Result<List<DynamicDto>> getUserDynamic(@PathVariable Long userId) {
        return ResultBuilder.onSuc(dynamicService.getUserDynamic(userId));
    }

    /**
     * 获取动态详情
     *
     * @param dynamicId
     * @return
     */
    @GetMapping("/dynamic/detail/{dynamicId}")
    public Result<Dynamic> getDetailDynamic(@PathVariable Long dynamicId) {
        return ResultBuilder.onSuc(dynamicService.getDetailDynamic(dynamicId));
    }

    /**
     * 发布动态
     *
     * @param postDynamicDto
     * @return
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/dynamic/post")
    public Result<String> postDynamic(@RequestBody PostDynamicDto postDynamicDto) {
        dynamicService.postDynamic(postDynamicDto);
        return ResultBuilder.onSuc();
    }

    /**
     * 上传动态照片
     *
     * @param
     * @return
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PutMapping("/dynamic/picture/upload")
    public Result<List<String>> uploadDynamicPicture(MultipartFile picture) {
        return ResultBuilder.onSuc(dynamicService.uploadDynamicPicture(picture));
    }

    /**
     * 删除动态
     *
     * @param dynamicId
     * @return
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/dynamic/{dynamicId}/delete")
    public Result<String> deleteDynamic(@PathVariable Long dynamicId) {

        dynamicService.deleteDynamic(dynamicId);
        return ResultBuilder.onSuc();
    }

    /**
     * 搜索动态
     *
     * @param searchType
     * @param key
     * @return
     */
    @GetMapping("/dynamic/search")
    public Result<List<DynamicDto>> searchDynamic(@RequestParam String searchType, @RequestParam String key) {
            return ResultBuilder.onSuc(dynamicService.searchDynamic(searchType, key));
    }

    /**
     * 评论动态
     *
     * @param postCommentDto
     * @return
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/dynamic/comment")
    public Result<String> commentDynamic(@RequestBody PostCommentDto postCommentDto) {
        if (postCommentDto.getLayer() != 0) {
            CheckUtils.notNull(postCommentDto.getLastLayerCommentId(), "子评论的父级评论ID不能为空");
        }
        dynamicService.validateCommentLayer(postCommentDto);
        dynamicService.commentDynamic(postCommentDto);
        return ResultBuilder.onSuc();
    }

    /**
     * 获取动态评论
     *
     * @param dynamicId
     * @return
     */
    @GetMapping("/dynamic/{dynamicId}/comment/all")
    public Result<List<DynamicCommentDto>> getDynamicComment(@PathVariable Long dynamicId) {
        return ResultBuilder.onSuc(dynamicService.getDynamicComment(dynamicId));
    }

    /**
     * 点赞动态
     *
     * @param dynamicId
     * @return
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/dynamic/{dynamicId}/like")
    public Result<String> likeDynamic(@PathVariable Long dynamicId) {

        dynamicService.likeDynamic(dynamicId);
        return ResultBuilder.onSuc();
    }


}
