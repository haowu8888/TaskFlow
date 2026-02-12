package com.taskflow.service;

import com.taskflow.dto.request.MemberInviteRequest;
import com.taskflow.dto.request.MemberRoleUpdateRequest;
import com.taskflow.dto.request.WorkspaceCreateRequest;
import com.taskflow.dto.request.WorkspaceUpdateRequest;
import com.taskflow.dto.response.WorkspaceMemberResponse;
import com.taskflow.dto.response.WorkspaceResponse;

import java.util.List;

public interface WorkspaceService {

    WorkspaceResponse createWorkspace(WorkspaceCreateRequest request, Long ownerId);

    WorkspaceResponse getWorkspace(Long id);

    List<WorkspaceResponse> listWorkspaces(Long userId);

    WorkspaceResponse updateWorkspace(Long id, WorkspaceUpdateRequest request);

    void deleteWorkspace(Long id);

    void inviteMember(Long workspaceId, MemberInviteRequest request);

    List<WorkspaceMemberResponse> getMembers(Long workspaceId);

    void updateMemberRole(Long workspaceId, Long userId, MemberRoleUpdateRequest request);

    void removeMember(Long workspaceId, Long userId);
}
