package com.chakfong.blog.dao;

import com.chakfong.blog.entity.DynamicComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DynamicCommentRepository extends JpaRepository<DynamicComment, Long> {

    List<DynamicComment> findDynamicCommentsByDynamic_DynamicIdIsOrderByCreateTimeAsc(Long dynamicId);

    DynamicComment findByDynamicCommentId(Long dynamicId);

    @Modifying
    @Query(value = "delete from dynamic_comment where dynamic_id = ?1 ", nativeQuery = true)
    Integer deleteAllByDynamic_DynamicId(Long dynamicId);
}
