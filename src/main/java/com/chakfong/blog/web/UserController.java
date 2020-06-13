package com.chakfong.blog.web;


import com.chakfong.blog.dto.request.SingleParamDto;
import com.chakfong.blog.dto.response.IsFriendDto;
import com.chakfong.blog.dto.response.Result;
import com.chakfong.blog.dto.response.ResultBuilder;
import com.chakfong.blog.dto.response.UserDto;
import com.chakfong.blog.entity.User;
import com.chakfong.blog.exception.ErrorCode;
import com.chakfong.blog.service.UserService;
import com.chakfong.blog.utils.CheckUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api")
@Slf4j
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }


    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/user")
    public Result<UserDto> getActualUser() {
        return ResultBuilder.onSuc(userService.getUserWithAuthorities().toDto());
    }

    /**
     * 获取个人信息
     *
     * @param userId
     * @return
     */
    @GetMapping("/user/{userId}")
    public Result<User> getUserById(@PathVariable long userId) {
        CheckUtils.notNull(userId, "用户名不能为空");
        return ResultBuilder.onSuc(userService.getUserById(userId));
    }

    /**
     * 上传头像
     *
     * @param avatar
     * @return
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PutMapping("/user/avatar/upload")
    public Result<String> setUserProfile(@RequestParam MultipartFile avatar) {
        return ResultBuilder.onSuc(userService.updateUserAvatar(avatar));
    }

    /**
     * 用户搜索
     *
     * @param searchType
     * @param key
     * @return
     */
    @GetMapping("/user/search")
    public Result<List<User>> searchUserByType(@RequestParam String searchType, @RequestParam String key) {

        CheckUtils.inList(searchType, Arrays.asList("username", "email"), "搜索类型必须为username或email");
        List<User> users = userService.searchUserByType(searchType, key);
        return ResultBuilder.onSuc(users);
    }

    /**
     * 添加好友
     *
     * @param userId
     * @return
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/user/friend/add")
    public Result<String> addUserFriend(@RequestBody SingleParamDto<Long> userId) {

        if (userService.addUserFriend(userId.get())) {
            return ResultBuilder.onSuc();
        } else {
            return ResultBuilder.onError(ErrorCode.INTERNAL_SERVER_ERROR.getCode(), "添加失败");
        }
    }

    /**
     * 获取好友列表
     *
     * @return
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/user/friend/all")
    public Result<List<User>> getAllFriend() {

        List<User> users = userService.getAllFriend();
        return ResultBuilder.onSuc(users);
    }

    /**
     * 删除好友
     *
     * @param userId
     * @return
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/user/friend/delete")
    public Result<String> deleteFriend(@RequestBody SingleParamDto<Long> userId) {

        if (userService.deleteFriend(userId.get())) {
            return ResultBuilder.onSuc();
        } else {
            return ResultBuilder.onError(ErrorCode.INTERNAL_SERVER_ERROR.getCode(), "删除失败");
        }
    }

    /**
     * 添加好友白名单
     *
     * @param userId
     * @return
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/user/whitelist/add")
    public Result<String> addWhitelist(@RequestBody SingleParamDto<Long> userId) {

        if (userService.addWhitelist(userId.get())) {
            return ResultBuilder.onSuc();
        } else {
            return ResultBuilder.onError(ErrorCode.INTERNAL_SERVER_ERROR.getCode(), "添加失败");
        }
    }

    /**
     * 添加好友黑名单
     *
     * @param userId
     * @return
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/user/blacklist/add")
    public Result<String> addBlacklist(@RequestBody SingleParamDto<Long> userId) {
        if (userService.addBlacklist(userId.get())) {
            return ResultBuilder.onSuc();
        } else {
            return ResultBuilder.onError(ErrorCode.INTERNAL_SERVER_ERROR.getCode(), "添加失败");
        }
    }

    /**
     * 允许好友添加
     *
     * @param userId
     * @return
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/user/friend/application/permit")
    public Result<String> permitFriendApplication(@RequestBody SingleParamDto<Long> userId) {
        if (userService.permitFriendApplication(userId.get())) {
            return ResultBuilder.onSuc();
        } else {
            return ResultBuilder.onError(ErrorCode.INTERNAL_SERVER_ERROR.getCode(), "请求失败");
        }
    }

    /**
     * 拒绝好友申请
     *
     * @param userId
     * @return
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/user/friend/application/reject")
    public Result<String> rejectFriendApplication(@RequestBody SingleParamDto<Long> userId) {
        if (userService.rejectFriendApplication(userId.get())) {
            return ResultBuilder.onSuc();
        } else {
            return ResultBuilder.onError(ErrorCode.INTERNAL_SERVER_ERROR.getCode(), "请求失败");
        }
    }

    /**
     * 获取好友申请列表
     *
     * @return
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/user/friend/application/all")
    public Result<List<User>> getAllFriendApplication() {
        List<User> users = userService.getAllFriendApplication();
        return ResultBuilder.onSuc(users);
    }

    /**
     * 是否为好友
     *
     * @param userIds
     * @return
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/user/isFriend")
    public Result<List<IsFriendDto>> isFriend(@RequestParam String userIds) {
        List<IsFriendDto> users = userService.isFriend(userIds);
        return ResultBuilder.onSuc(users);
    }


}
