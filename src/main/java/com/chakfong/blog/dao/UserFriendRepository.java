package com.chakfong.blog.dao;

import com.chakfong.blog.entity.UserFriend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserFriendRepository extends JpaRepository<UserFriend, Long> {

    @Modifying
    @Query(value = "select * from user_friend where to_user_id = ?1 union all" +
            " select * from user_friend where from_user_id = ?1  ", nativeQuery = true)
    List<UserFriend> findAllFriendByUserId(Long userId);

    Integer deleteUserFriendByFromUserIdIsAndToUserIdIs(Long fromUserId, Long toUserId);

    Integer countDistinctByFromUserIdIsAndToUserIdIs(Long fromUserId, Long toUserId);

}