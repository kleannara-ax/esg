package com.company.module.user.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * JWT 토큰 응답 DTO
 */
@Getter
@Builder
public class TokenResponse {

    private String accessToken;
    private String tokenType;
    private long expiresIn;

    public static TokenResponse of(String accessToken, long expiresInMs) {
        return TokenResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(expiresInMs / 1000)   // 초 단위
                .build();
    }
}
