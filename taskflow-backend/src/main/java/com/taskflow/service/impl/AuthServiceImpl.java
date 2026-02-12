package com.taskflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.taskflow.dto.request.LoginRequest;
import com.taskflow.dto.request.RefreshTokenRequest;
import com.taskflow.dto.request.RegisterRequest;
import com.taskflow.dto.response.AuthResponse;
import com.taskflow.dto.response.UserResponse;
import com.taskflow.entity.RefreshToken;
import com.taskflow.entity.User;
import com.taskflow.entity.Workspace;
import com.taskflow.entity.WorkspaceMember;
import com.taskflow.enums.MemberRole;
import com.taskflow.exception.BusinessException;
import com.taskflow.mapper.RefreshTokenMapper;
import com.taskflow.mapper.UserMapper;
import com.taskflow.mapper.WorkspaceMapper;
import com.taskflow.mapper.WorkspaceMemberMapper;
import com.taskflow.security.JwtTokenProvider;
import com.taskflow.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final RefreshTokenMapper refreshTokenMapper;
    private final WorkspaceMapper workspaceMapper;
    private final WorkspaceMemberMapper workspaceMemberMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check duplicate username
        LambdaQueryWrapper<User> usernameQuery = new LambdaQueryWrapper<>();
        usernameQuery.eq(User::getUsername, request.getUsername());
        if (userMapper.selectCount(usernameQuery) > 0) {
            throw new BusinessException("Username already exists");
        }

        // Check duplicate email
        LambdaQueryWrapper<User> emailQuery = new LambdaQueryWrapper<>();
        emailQuery.eq(User::getEmail, request.getEmail());
        if (userMapper.selectCount(emailQuery) > 0) {
            throw new BusinessException("Email already exists");
        }

        // Create user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setDeleted(0);
        userMapper.insert(user);

        // Create default workspace "My Workspace"
        Workspace workspace = new Workspace();
        workspace.setName("My Workspace");
        workspace.setDescription("Default workspace");
        workspace.setOwnerId(user.getId());
        workspace.setCreatedAt(LocalDateTime.now());
        workspace.setUpdatedAt(LocalDateTime.now());
        workspace.setDeleted(0);
        workspaceMapper.insert(workspace);

        // Add owner as OWNER member
        WorkspaceMember member = new WorkspaceMember();
        member.setWorkspaceId(workspace.getId());
        member.setUserId(user.getId());
        member.setRole(MemberRole.OWNER.name());
        member.setJoinedAt(LocalDateTime.now());
        workspaceMemberMapper.insert(member);

        // Generate tokens
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getUsername());
        String refreshTokenValue = jwtTokenProvider.generateRefreshTokenValue();

        // Save refresh token
        saveRefreshToken(user.getId(), refreshTokenValue);

        UserResponse userResponse = toUserResponse(user);
        return new AuthResponse(accessToken, refreshTokenValue, userResponse);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        // Find user by username
        LambdaQueryWrapper<User> query = new LambdaQueryWrapper<>();
        query.eq(User::getUsername, request.getUsername());
        User user = userMapper.selectOne(query);

        if (user == null) {
            throw new BusinessException("Invalid username or password");
        }

        // Check password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException("Invalid username or password");
        }

        // Generate tokens
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getUsername());
        String refreshTokenValue = jwtTokenProvider.generateRefreshTokenValue();

        // Save refresh token
        saveRefreshToken(user.getId(), refreshTokenValue);

        UserResponse userResponse = toUserResponse(user);
        return new AuthResponse(accessToken, refreshTokenValue, userResponse);
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        // Find token in DB
        LambdaQueryWrapper<RefreshToken> query = new LambdaQueryWrapper<>();
        query.eq(RefreshToken::getToken, request.getRefreshToken());
        RefreshToken refreshToken = refreshTokenMapper.selectOne(query);

        if (refreshToken == null) {
            throw new BusinessException("Invalid refresh token");
        }

        // Check not expired
        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Refresh token expired");
        }

        // Check not revoked
        if (refreshToken.getIsRevoked() != null && refreshToken.getIsRevoked() == 1) {
            throw new BusinessException("Refresh token has been revoked");
        }

        // Get user
        User user = userMapper.selectById(refreshToken.getUserId());
        if (user == null) {
            throw new BusinessException("User not found");
        }

        // Generate new access token
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getUsername());

        UserResponse userResponse = toUserResponse(user);
        return new AuthResponse(accessToken, request.getRefreshToken(), userResponse);
    }

    @Override
    public void logout(RefreshTokenRequest request) {
        // Find token
        LambdaQueryWrapper<RefreshToken> query = new LambdaQueryWrapper<>();
        query.eq(RefreshToken::getToken, request.getRefreshToken());
        RefreshToken refreshToken = refreshTokenMapper.selectOne(query);

        if (refreshToken != null) {
            // Mark revoked
            refreshToken.setIsRevoked(1);
            refreshTokenMapper.updateById(refreshToken);
        }
    }

    private void saveRefreshToken(Long userId, String tokenValue) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserId(userId);
        refreshToken.setToken(tokenValue);
        refreshToken.setExpiresAt(LocalDateTime.now().plusSeconds(jwtTokenProvider.getRefreshTokenExpiration() / 1000));
        refreshToken.setIsRevoked(0);
        refreshToken.setCreatedAt(LocalDateTime.now());
        refreshTokenMapper.insert(refreshToken);
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
