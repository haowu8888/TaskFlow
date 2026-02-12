package com.taskflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.taskflow.dto.request.MemberInviteRequest;
import com.taskflow.dto.request.MemberRoleUpdateRequest;
import com.taskflow.dto.request.WorkspaceCreateRequest;
import com.taskflow.dto.request.WorkspaceUpdateRequest;
import com.taskflow.dto.response.UserResponse;
import com.taskflow.dto.response.WorkspaceMemberResponse;
import com.taskflow.dto.response.WorkspaceResponse;
import com.taskflow.entity.User;
import com.taskflow.entity.Workspace;
import com.taskflow.entity.WorkspaceMember;
import com.taskflow.enums.MemberRole;
import com.taskflow.exception.BusinessException;
import com.taskflow.mapper.UserMapper;
import com.taskflow.mapper.WorkspaceMapper;
import com.taskflow.mapper.WorkspaceMemberMapper;
import com.taskflow.service.WorkspaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkspaceServiceImpl implements WorkspaceService {

    private final WorkspaceMapper workspaceMapper;
    private final WorkspaceMemberMapper workspaceMemberMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public WorkspaceResponse createWorkspace(WorkspaceCreateRequest request, Long ownerId) {
        Workspace workspace = new Workspace();
        workspace.setName(request.getName());
        workspace.setDescription(request.getDescription());
        workspace.setOwnerId(ownerId);
        workspace.setCreatedAt(LocalDateTime.now());
        workspace.setUpdatedAt(LocalDateTime.now());
        workspace.setDeleted(0);
        workspaceMapper.insert(workspace);

        // Add owner as OWNER member
        WorkspaceMember member = new WorkspaceMember();
        member.setWorkspaceId(workspace.getId());
        member.setUserId(ownerId);
        member.setRole(MemberRole.OWNER.name());
        member.setJoinedAt(LocalDateTime.now());
        workspaceMemberMapper.insert(member);

        return buildWorkspaceResponse(workspace);
    }

    @Override
    public WorkspaceResponse getWorkspace(Long id) {
        Workspace workspace = workspaceMapper.selectById(id);
        if (workspace == null) {
            throw new BusinessException("Workspace not found");
        }
        return buildWorkspaceResponse(workspace);
    }

    @Override
    public List<WorkspaceResponse> listWorkspaces(Long userId) {
        // Get workspace IDs where user is a member
        LambdaQueryWrapper<WorkspaceMember> memberQuery = new LambdaQueryWrapper<>();
        memberQuery.eq(WorkspaceMember::getUserId, userId);
        List<WorkspaceMember> memberships = workspaceMemberMapper.selectList(memberQuery);

        if (memberships.isEmpty()) {
            return List.of();
        }

        List<Long> workspaceIds = memberships.stream()
                .map(WorkspaceMember::getWorkspaceId)
                .collect(Collectors.toList());

        LambdaQueryWrapper<Workspace> workspaceQuery = new LambdaQueryWrapper<>();
        workspaceQuery.in(Workspace::getId, workspaceIds);
        workspaceQuery.orderByDesc(Workspace::getCreatedAt);
        List<Workspace> workspaces = workspaceMapper.selectList(workspaceQuery);

        return workspaces.stream().map(this::buildWorkspaceResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public WorkspaceResponse updateWorkspace(Long id, WorkspaceUpdateRequest request) {
        Workspace workspace = workspaceMapper.selectById(id);
        if (workspace == null) {
            throw new BusinessException("Workspace not found");
        }

        if (StringUtils.hasText(request.getName())) {
            workspace.setName(request.getName());
        }
        if (request.getDescription() != null) {
            workspace.setDescription(request.getDescription());
        }
        workspace.setUpdatedAt(LocalDateTime.now());
        workspaceMapper.updateById(workspace);

        return buildWorkspaceResponse(workspace);
    }

    @Override
    public void deleteWorkspace(Long id) {
        Workspace workspace = workspaceMapper.selectById(id);
        if (workspace == null) {
            throw new BusinessException("Workspace not found");
        }
        workspaceMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void inviteMember(Long workspaceId, MemberInviteRequest request) {
        // Check workspace exists
        Workspace workspace = workspaceMapper.selectById(workspaceId);
        if (workspace == null) {
            throw new BusinessException("Workspace not found");
        }

        // Check user exists
        User user = userMapper.selectById(request.getUserId());
        if (user == null) {
            throw new BusinessException("User not found");
        }

        // Check not already a member
        LambdaQueryWrapper<WorkspaceMember> query = new LambdaQueryWrapper<>();
        query.eq(WorkspaceMember::getWorkspaceId, workspaceId);
        query.eq(WorkspaceMember::getUserId, request.getUserId());
        if (workspaceMemberMapper.selectCount(query) > 0) {
            throw new BusinessException("User is already a member of this workspace");
        }

        WorkspaceMember member = new WorkspaceMember();
        member.setWorkspaceId(workspaceId);
        member.setUserId(request.getUserId());
        member.setRole(request.getRole());
        member.setJoinedAt(LocalDateTime.now());
        workspaceMemberMapper.insert(member);
    }

    @Override
    public List<WorkspaceMemberResponse> getMembers(Long workspaceId) {
        LambdaQueryWrapper<WorkspaceMember> query = new LambdaQueryWrapper<>();
        query.eq(WorkspaceMember::getWorkspaceId, workspaceId);
        List<WorkspaceMember> members = workspaceMemberMapper.selectList(query);

        return members.stream().map(member -> {
            User user = userMapper.selectById(member.getUserId());
            WorkspaceMemberResponse response = new WorkspaceMemberResponse();
            response.setId(member.getId());
            response.setUserId(member.getUserId());
            response.setRole(member.getRole());
            response.setJoinedAt(member.getJoinedAt());
            if (user != null) {
                response.setUsername(user.getUsername());
                response.setEmail(user.getEmail());
                response.setAvatarUrl(user.getAvatarUrl());
            }
            return response;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateMemberRole(Long workspaceId, Long userId, MemberRoleUpdateRequest request) {
        LambdaQueryWrapper<WorkspaceMember> query = new LambdaQueryWrapper<>();
        query.eq(WorkspaceMember::getWorkspaceId, workspaceId);
        query.eq(WorkspaceMember::getUserId, userId);
        WorkspaceMember member = workspaceMemberMapper.selectOne(query);

        if (member == null) {
            throw new BusinessException("Member not found");
        }

        member.setRole(request.getRole());
        workspaceMemberMapper.updateById(member);
    }

    @Override
    @Transactional
    public void removeMember(Long workspaceId, Long userId) {
        LambdaQueryWrapper<WorkspaceMember> query = new LambdaQueryWrapper<>();
        query.eq(WorkspaceMember::getWorkspaceId, workspaceId);
        query.eq(WorkspaceMember::getUserId, userId);
        WorkspaceMember member = workspaceMemberMapper.selectOne(query);

        if (member == null) {
            throw new BusinessException("Member not found");
        }

        // Prevent removing owner
        if (MemberRole.OWNER.name().equals(member.getRole())) {
            throw new BusinessException("Cannot remove workspace owner");
        }

        workspaceMemberMapper.deleteById(member.getId());
    }

    private WorkspaceResponse buildWorkspaceResponse(Workspace workspace) {
        WorkspaceResponse response = new WorkspaceResponse();
        response.setId(workspace.getId());
        response.setName(workspace.getName());
        response.setDescription(workspace.getDescription());
        response.setCreatedAt(workspace.getCreatedAt());

        // Fetch owner
        if (workspace.getOwnerId() != null) {
            User owner = userMapper.selectById(workspace.getOwnerId());
            if (owner != null) {
                response.setOwner(new UserResponse(
                        owner.getId(), owner.getUsername(), owner.getEmail(), owner.getAvatarUrl()));
            }
        }

        // Get member count
        LambdaQueryWrapper<WorkspaceMember> memberQuery = new LambdaQueryWrapper<>();
        memberQuery.eq(WorkspaceMember::getWorkspaceId, workspace.getId());
        Long memberCount = workspaceMemberMapper.selectCount(memberQuery);
        response.setMemberCount(memberCount.intValue());

        return response;
    }
}
