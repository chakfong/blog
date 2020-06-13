package com.chakfong.blog.dao;

import com.chakfong.blog.entity.DynamicPicture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DynamicPictureRepository extends JpaRepository<DynamicPicture, Long> {


    @Modifying
    @Query(value = "delete from dynamic_picture where dynamic_id = ?1 ", nativeQuery = true)
    Integer deleteAllByDynamic_DynamicIdIs(Long dynamicId);

    @Query(value = "select * from dynamic_picture where dynamic_id = ?1", nativeQuery = true)
    List<DynamicPicture> findDynamicPicture(Long dynamicId);
}
