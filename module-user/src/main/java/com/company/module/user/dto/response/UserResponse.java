package com.company.module.user.dto.response;

import com.company.module.user.entity.User;
import com.company.module.user.entity.UserRole;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 사용자 응답 DTO
 */
@Getter
@Builder
public class UserResponse {

    private Long userId;
    private String loginId;
    private String userName;
    private String email;
    private UserRole role;
    private boolean activeYn;
    private LocalDateTime createdAt;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .loginId(user.getLoginId())
                .userName(user.getUserName())
                .email(user.getEmail())
                .role(user.getRole())
                .activeYn(user.isActiveYn())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
