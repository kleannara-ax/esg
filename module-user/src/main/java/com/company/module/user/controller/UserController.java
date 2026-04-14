package com.company.module.user.controller;

import com.company.common.response.ApiResponse;
import com.company.module.user.dto.response.UserResponse;
import com.company.module.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 사용자 관리 Controller
 * <p>URL Prefix: /user-api/users</p>
 */
@RestController
@RequestMapping("/user-api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * GET /user-api/users
     * 전체 사용자 조회 (ADMIN 전용)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<UserResponse>> getAllUsers() {
        return ApiResponse.ok(userService.getAllUsers());
    }

    /**
     * GET /user-api/users/{userId}
     * 사용자 단건 조회
     */
    @GetMapping("/{userId}")
    public ApiResponse<UserResponse> getUser(@PathVariable Long userId) {
        return ApiResponse.ok(userService.getUser(userId));
    }

    /**
     * DELETE /user-api/users/{userId}
     * 사용자 비활성화 (ADMIN 전용)
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deactivateUser(@PathVariable Long userId) {
        userService.deactivateUser(userId);
        return ApiResponse.ok("사용자가 비활성화되었습니다.", null);
    }
}
