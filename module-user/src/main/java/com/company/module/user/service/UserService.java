package com.company.module.user.service;

import com.company.exception.BusinessException;
import com.company.exception.ErrorCode;
import com.company.module.user.dto.request.SignupRequest;
import com.company.module.user.dto.response.UserResponse;
import com.company.module.user.entity.User;
import com.company.module.user.entity.UserRole;
import com.company.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 사용자 관리 Service
 * <p>@Transactional 은 Service 계층에서만 사용한다.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /** 회원가입 */
    @Transactional
    public UserResponse signup(SignupRequest request) {
        validateDuplicateUser(request.getLoginId(), request.getEmail());

        User user = User.builder()
                .loginId(request.getLoginId())
                .password(passwordEncoder.encode(request.getPassword()))
                .userName(request.getUserName())
                .email(request.getEmail())
                .role(UserRole.ROLE_USER)
                .activeYn(true)
                .build();

        User saved = userRepository.save(user);
        log.info("신규 사용자 등록 완료: loginId={}", saved.getLoginId());
        return UserResponse.from(saved);
    }

    /** 사용자 단건 조회 */
    @Transactional(readOnly = true)
    public UserResponse getUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return UserResponse.from(user);
    }

    /** 전체 사용자 조회 */
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserResponse::from)
                .toList();
    }

    /** 사용자 비활성화 */
    @Transactional
    public void deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        user.deactivate();
        log.info("사용자 비활성화: userId={}", userId);
    }

    // ── private helpers ──────────────────────────────────────────────────────

    private void validateDuplicateUser(String loginId, String email) {
        if (userRepository.existsByLoginId(loginId)) {
            throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS,
                    "이미 사용 중인 로그인 ID 입니다: " + loginId);
        }
        if (email != null && userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE,
                    "이미 사용 중인 이메일 입니다: " + email);
        }
    }
}
