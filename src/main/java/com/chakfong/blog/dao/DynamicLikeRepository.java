package com.chakfong.blog.dao;

import com.chakfong.blog.entity.DynamicLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DynamicLikeRepository extends JpaRepository<DynamicLike, Long> {


    Integer deleteAllByDynamicIdIs(Long dynamicId);

    Integer countByDynamicIdIsAndUserIdIs(Long dynamicId, Long userId);
}
