package com.taskflow.controller;

import com.taskflow.dto.request.LoginRequest;
import com.taskflow.dto.request.RefreshTokenRequest;
import com.taskflow.dto.request.RegisterRequest;
import com.taskflow.dto.response.ApiResponse;
import com.taskflow.dto.response.AuthResponse;
import com.taskflow.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Validated @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ApiResponse.success(response);
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Validated @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ApiResponse.success(response);
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refreshToken(@Validated @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return ApiResponse.success(response);
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@Validated @RequestBody RefreshTokenRequest request) {
        authService.logout(request);
        return ApiResponse.success();
    }
}
