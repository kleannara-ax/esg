package com.company.module.user.repository;

import com.company.module.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * 사용자 Repository
 */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByLoginId(String loginId);

    Optional<User> findByEmail(String email);

    boolean existsByLoginId(String loginId);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.loginId = :loginId AND u.activeYn = true")
    Optional<User> findActiveUserByLoginId(@Param("loginId") String loginId);
}
