package com.company.module.user.entity;

import com.company.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 사용자 엔티티
 * <p>테이블명 prefix: MOD_USER_</p>
 */
@Entity
@Table(name = "MOD_USER_ACCOUNT")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    /** 로그인 ID */
    @Column(name = "login_id", length = 50, nullable = false, unique = true)
    private String loginId;

    /** 비밀번호 (BCrypt 해시) */
    @Column(name = "password", length = 255, nullable = false)
    private String password;

    /** 사용자명 */
    @Column(name = "user_name", length = 100, nullable = false)
    private String userName;

    /** 이메일 */
    @Column(name = "email", length = 200, unique = true)
    private String email;

    /** 역할 */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20, nullable = false)
    private UserRole role;

    /** 활성화 여부 */
    @Column(name = "active_yn", nullable = false)
    private boolean activeYn;

    // ── 비즈니스 메서드 ──────────────────────────────────────────────────────

    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void updateProfile(String userName, String email) {
        this.userName = userName;
        this.email    = email;
    }

    public void deactivate() {
        this.activeYn = false;
    }
}
