package com.company.module.user.service;

import com.company.exception.BusinessException;
import com.company.module.user.dto.request.SignupRequest;
import com.company.module.user.dto.response.UserResponse;
import com.company.module.user.entity.User;
import com.company.module.user.entity.UserRole;
import com.company.module.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 단위 테스트")
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .loginId("testuser")
                .password("$2a$10$encodedPassword")
                .userName("테스트사용자")
                .email("test@company.com")
                .role(UserRole.ROLE_USER)
                .activeYn(true)
                .build();
    }

    @Nested
    @DisplayName("회원가입")
    class Signup {

        @Test
        @DisplayName("정상 회원가입 성공")
        void signup_success() {
            // given
            SignupRequest request = buildSignupRequest("newuser", "test@company.com");
            given(userRepository.existsByLoginId("newuser")).willReturn(false);
            given(userRepository.existsByEmail("test@company.com")).willReturn(false);
            given(passwordEncoder.encode(anyString())).willReturn("encoded_password");
            given(userRepository.save(any(User.class))).willReturn(sampleUser);

            // when
            UserResponse response = userService.signup(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getLoginId()).isEqualTo("testuser");
            then(userRepository).should(times(1)).save(any(User.class));
        }

        @Test
        @DisplayName("중복 loginId 회원가입 시 예외 발생")
        void signup_duplicateLoginId_throws() {
            // given
            SignupRequest request = buildSignupRequest("testuser", "other@company.com");
            given(userRepository.existsByLoginId("testuser")).willReturn(true);

            // when & then
            assertThatThrownBy(() -> userService.signup(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("testuser");
        }
    }

    @Nested
    @DisplayName("사용자 조회")
    class GetUser {

        @Test
        @DisplayName("존재하는 사용자 조회 성공")
        void getUser_success() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(sampleUser));

            // when
            UserResponse response = userService.getUser(1L);

            // then
            assertThat(response.getLoginId()).isEqualTo("testuser");
        }

        @Test
        @DisplayName("존재하지 않는 사용자 조회 시 예외 발생")
        void getUser_notFound_throws() {
            // given
            given(userRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.getUser(999L))
                    .isInstanceOf(BusinessException.class);
        }
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private SignupRequest buildSignupRequest(String loginId, String email) {
        try {
            SignupRequest req = new SignupRequest();
            setField(req, "loginId",  loginId);
            setField(req, "password", "Password123!");
            setField(req, "userName", "테스트사용자");
            setField(req, "email",    email);
            return req;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setField(Object obj, String fieldName, Object value) throws Exception {
        var field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }
}
