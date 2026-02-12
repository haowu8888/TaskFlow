package com.taskflow.controller;

import com.taskflow.dto.request.MemberInviteRequest;
import com.taskflow.dto.request.MemberRoleUpdateRequest;
import com.taskflow.dto.request.WorkspaceCreateRequest;
import com.taskflow.dto.request.WorkspaceUpdateRequest;
import com.taskflow.dto.response.ApiResponse;
import com.taskflow.dto.response.WorkspaceMemberResponse;
import com.taskflow.dto.response.WorkspaceResponse;
import com.taskflow.service.WorkspaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @PostMapping
    public ApiResponse<WorkspaceResponse> createWorkspace(@Validated @RequestBody WorkspaceCreateRequest request) {
        Long ownerId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        WorkspaceResponse response = workspaceService.createWorkspace(request, ownerId);
        return ApiResponse.success(response);
    }

    @GetMapping
    public ApiResponse<List<WorkspaceResponse>> listWorkspaces() {
        Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        List<WorkspaceResponse> response = workspaceService.listWorkspaces(userId);
        return ApiResponse.success(response);
    }

    @GetMapping("/{id}")
    public ApiResponse<WorkspaceResponse> getWorkspace(@PathVariable Long id) {
        WorkspaceResponse response = workspaceService.getWorkspace(id);
        return ApiResponse.success(response);
    }

    @PutMapping("/{id}")
    public ApiResponse<WorkspaceResponse> updateWorkspace(
            @PathVariable Long id,
            @RequestBody WorkspaceUpdateRequest request) {
        WorkspaceResponse response = workspaceService.updateWorkspace(id, request);
        return ApiResponse.success(response);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteWorkspace(@PathVariable Long id) {
        workspaceService.deleteWorkspace(id);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/members")
    public ApiResponse<Void> inviteMember(
            @PathVariable Long id,
            @Validated @RequestBody MemberInviteRequest request) {
        workspaceService.inviteMember(id, request);
        return ApiResponse.success();
    }

    @GetMapping("/{id}/members")
    public ApiResponse<List<WorkspaceMemberResponse>> getMembers(@PathVariable Long id) {
        List<WorkspaceMemberResponse> response = workspaceService.getMembers(id);
        return ApiResponse.success(response);
    }

    @PutMapping("/{id}/members/{userId}/role")
    public ApiResponse<Void> updateMemberRole(
            @PathVariable Long id,
            @PathVariable Long userId,
            @Validated @RequestBody MemberRoleUpdateRequest request) {
        workspaceService.updateMemberRole(id, userId, request);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}/members/{userId}")
    public ApiResponse<Void> removeMember(
            @PathVariable Long id,
            @PathVariable Long userId) {
        workspaceService.removeMember(id, userId);
        return ApiResponse.success();
    }
}
