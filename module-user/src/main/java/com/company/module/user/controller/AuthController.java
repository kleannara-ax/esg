package com.company.module.user.controller;

import com.company.common.response.ApiResponse;
import com.company.module.user.dto.request.LoginRequest;
import com.company.module.user.dto.request.SignupRequest;
import com.company.module.user.dto.response.TokenResponse;
import com.company.module.user.dto.response.UserResponse;
import com.company.module.user.service.AuthService;
import com.company.module.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 Controller
 * <p>URL Prefix: /user-api/auth</p>
 */
@RestController
@RequestMapping("/user-api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    /**
     * POST /user-api/auth/signup
     * 회원가입 (SecurityConfig 에서 permitAll)
     */
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ApiResponse.created(userService.signup(request));
    }

    /**
     * POST /user-api/auth/login
     * 로그인 (SecurityConfig 에서 permitAll)
     */
    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }
}
