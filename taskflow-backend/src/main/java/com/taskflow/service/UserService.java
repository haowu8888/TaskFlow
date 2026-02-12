package com.taskflow.service;

import com.taskflow.dto.response.UserResponse;
import com.taskflow.entity.User;

import java.util.List;

public interface UserService {
    UserResponse getCurrentUser(Long userId);
    UserResponse updateProfile(Long userId, String avatarUrl);
    List<UserResponse> searchUsers(String keyword);
    User getUserById(Long userId);
}
