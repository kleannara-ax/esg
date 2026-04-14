package com.company.module.user.service;

import com.company.exception.BusinessException;
import com.company.exception.ErrorCode;
import com.company.module.user.dto.request.LoginRequest;
import com.company.module.user.dto.response.TokenResponse;
import com.company.module.user.entity.User;
import com.company.module.user.repository.UserRepository;
import com.company.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 인증 Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${jwt.expiration-ms:86400000}")
    private long expirationMs;

    /** 로그인 → JWT 토큰 발급 */
    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findActiveUserByLoginId(request.getLoginId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getLoginId(),
                null,
                List.of(new SimpleGrantedAuthority(user.getRole().name()))
        );

        String token = jwtTokenProvider.createToken(authentication);
        log.info("로그인 성공: loginId={}", user.getLoginId());
        return TokenResponse.of(token, expirationMs);
    }
}
