package com.chakfong.blog.dao;

import com.chakfong.blog.entity.UserFriendApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserFriendApplicationRepository extends JpaRepository<UserFriendApplication, Long> {

    UserFriendApplication findUserFriendApplicationByFromUserIdIsAndToUserIdIs(Long fromUserId, Long toUserId);

    List<UserFriendApplication> findUserFriendApplicationsByToUserIdIs(Long toUserId);

    Integer deleteUserFriendApplicationByFromUserIdIsAndToUserIdIs(Long fromUserId, Long toUserId);
}
