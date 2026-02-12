package com.taskflow.controller;

import com.taskflow.dto.request.LabelCreateRequest;
import com.taskflow.dto.request.LabelUpdateRequest;
import com.taskflow.dto.response.ApiResponse;
import com.taskflow.dto.response.LabelResponse;
import com.taskflow.service.LabelService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class LabelController {

    private final LabelService labelService;

    @PostMapping("/api/v1/workspaces/{workspaceId}/labels")
    public ApiResponse<LabelResponse> createLabel(
            @PathVariable Long workspaceId,
            @Validated @RequestBody LabelCreateRequest request) {
        Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        LabelResponse response = labelService.createLabel(workspaceId, userId, request);
        return ApiResponse.success(response);
    }

    @GetMapping("/api/v1/workspaces/{workspaceId}/labels")
    public ApiResponse<List<LabelResponse>> listLabels(@PathVariable Long workspaceId) {
        List<LabelResponse> response = labelService.listLabels(workspaceId);
        return ApiResponse.success(response);
    }

    @PutMapping("/api/v1/labels/{id}")
    public ApiResponse<LabelResponse> updateLabel(
            @PathVariable Long id,
            @Validated @RequestBody LabelUpdateRequest request) {
        LabelResponse response = labelService.updateLabel(id, request);
        return ApiResponse.success(response);
    }

    @DeleteMapping("/api/v1/labels/{id}")
    public ApiResponse<Void> deleteLabel(@PathVariable Long id) {
        labelService.deleteLabel(id);
        return ApiResponse.success();
    }

    @GetMapping("/api/v1/tasks/{taskId}/labels")
    public ApiResponse<List<LabelResponse>> getTaskLabels(@PathVariable Long taskId) {
        List<LabelResponse> response = labelService.getTaskLabels(taskId);
        return ApiResponse.success(response);
    }

    @PostMapping("/api/v1/tasks/{taskId}/labels/{labelId}")
    public ApiResponse<Void> addLabelToTask(@PathVariable Long taskId, @PathVariable Long labelId) {
        labelService.addLabelToTask(taskId, labelId);
        return ApiResponse.success();
    }

    @DeleteMapping("/api/v1/tasks/{taskId}/labels/{labelId}")
    public ApiResponse<Void> removeLabelFromTask(@PathVariable Long taskId, @PathVariable Long labelId) {
        labelService.removeLabelFromTask(taskId, labelId);
        return ApiResponse.success();
    }
}
