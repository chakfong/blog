package com.chakfong.blog.dao;

import com.chakfong.blog.entity.Dynamic;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DynamicRepository extends JpaRepository<Dynamic, Long> {

    List<Dynamic> findDynamicsByVisibleIsOrderByCreateTime(Integer visible);

    List<Dynamic> findDynamicsByUser_UserIdIs(Long userId);

    List<Dynamic> findDistinctDynamicsByUser_UserIdIn(List<Long> userIds);

    Dynamic findDynamicByDynamicIdIs(Long dynamicId);

    List<Dynamic> findDynamicsByContentContaining(String content);

    List<Dynamic> findDynamicsByTitleContaining(String title);

    List<Dynamic> findDynamicsByContentContainingOrTitleContaining(String content,String title);

    @Modifying
    @Query(value = "update dynamic set like_count = like_count + 1 where dynamic_id = ?1 ", nativeQuery = true)
    Integer updateLikeCount(Long dynamicId);
}
