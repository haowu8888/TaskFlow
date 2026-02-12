package com.taskflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.taskflow.dto.response.UserResponse;
import com.taskflow.entity.User;
import com.taskflow.exception.BusinessException;
import com.taskflow.mapper.UserMapper;
import com.taskflow.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    @Override
    public UserResponse getCurrentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("User not found");
        }
        return toUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(Long userId, String avatarUrl) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("User not found");
        }

        if (avatarUrl != null) {
            user.setAvatarUrl(avatarUrl);
        }
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);

        return toUserResponse(user);
    }

    @Override
    public List<UserResponse> searchUsers(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return List.of();
        }

        LambdaQueryWrapper<User> query = new LambdaQueryWrapper<>();
        query.like(User::getUsername, keyword)
             .or()
             .like(User::getEmail, keyword);
        query.last("LIMIT 20");
        List<User> users = userMapper.selectList(query);

        return users.stream()
                .map(this::toUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    public User getUserById(Long userId) {
        return userMapper.selectById(userId);
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getAvatarUrl()
        );
    }
}
