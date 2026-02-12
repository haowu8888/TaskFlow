package com.taskflow.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.taskflow.dto.response.ApiResponse;
import com.taskflow.dto.response.UserResponse;
import com.taskflow.entity.User;
import com.taskflow.exception.BusinessException;
import com.taskflow.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/me")
    public ApiResponse<UserResponse> getCurrentUser() {
        Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("User not found");
        }
        UserResponse response = new UserResponse(
                user.getId(), user.getUsername(), user.getEmail(), user.getAvatarUrl());
        return ApiResponse.success(response);
    }

    @PutMapping("/me")
    public ApiResponse<UserResponse> updateProfile(@RequestBody Map<String, String> request) {
        Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("User not found");
        }

        if (request.containsKey("avatarUrl")) {
            user.setAvatarUrl(request.get("avatarUrl"));
        }
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);

        UserResponse response = new UserResponse(
                user.getId(), user.getUsername(), user.getEmail(), user.getAvatarUrl());
        return ApiResponse.success(response);
    }

    @GetMapping("/search")
    public ApiResponse<List<UserResponse>> searchUsers(@RequestParam(required = false) String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return ApiResponse.success(List.of());
        }

        LambdaQueryWrapper<User> query = new LambdaQueryWrapper<>();
        query.like(User::getUsername, keyword)
             .or()
             .like(User::getEmail, keyword);
        query.last("LIMIT 20");
        List<User> users = userMapper.selectList(query);

        List<UserResponse> responses = users.stream()
                .map(u -> new UserResponse(u.getId(), u.getUsername(), u.getEmail(), u.getAvatarUrl()))
                .collect(Collectors.toList());
        return ApiResponse.success(responses);
    }

    @PutMapping("/me/password")
    public ApiResponse<Void> changePassword(@RequestBody Map<String, String> request) {
        Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("User not found");
        }

        String currentPassword = request.get("currentPassword");
        String newPassword = request.get("newPassword");

        if (!StringUtils.hasText(currentPassword) || !StringUtils.hasText(newPassword)) {
            throw new BusinessException("Current password and new password are required");
        }

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new BusinessException("Current password is incorrect");
        }

        if (newPassword.length() < 6) {
            throw new BusinessException("New password must be at least 6 characters");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);

        return ApiResponse.success(null);
    }
}
