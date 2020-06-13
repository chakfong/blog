package com.chakfong.blog.dao;

import com.chakfong.blog.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

   @EntityGraph(attributePaths = "authorities")
   Optional<User> findOneWithAuthoritiesByUsername(String username);

   @EntityGraph(attributePaths = "authorities")
   Optional<User> findOneWithAuthoritiesByEmailIgnoreCase(String email);

   User findUserByUserId(Long UserId);

   User findUserByEmail(String email);

   User findUserByUsername(String username);

   List<User> findUsersByUsernameContaining(String key);

   List<User> findUsersByEmailContaining(String key);

   List<User> findUsersByUserIdIn(List<Long> userIds);

   @Query(value = "select from_user_id from user_whitelist where to_user_id = ?1 ", nativeQuery = true)
   List<Long> findFromUserIdByToUserIdInWhitelist(Long userId);

   @Query(value = "select count(*) from user_whitelist where from_user_id = ?1 and to_user_id = ?2 ", nativeQuery = true)
   Integer findWhitelistRecord(Long fromUserId, Long toUserId);

   @Query(value = "select count(*) from user_blacklist where from_user_id = ?1 and to_user_id = ?2 ", nativeQuery = true)
   Integer findBlacklistRecord(Long fromUserId, Long toUserId);

}
