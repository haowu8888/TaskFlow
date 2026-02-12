package com.taskflow.controller;

import com.taskflow.dto.request.SubtaskCreateRequest;
import com.taskflow.dto.request.SubtaskReorderRequest;
import com.taskflow.dto.request.SubtaskUpdateRequest;
import com.taskflow.dto.response.ApiResponse;
import com.taskflow.dto.response.SubtaskResponse;
import com.taskflow.service.SubtaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SubtaskController {

    private final SubtaskService subtaskService;

    @GetMapping("/api/v1/tasks/{taskId}/subtasks")
    public ApiResponse<List<SubtaskResponse>> listSubtasks(@PathVariable Long taskId) {
        List<SubtaskResponse> response = subtaskService.listSubtasks(taskId);
        return ApiResponse.success(response);
    }

    @PostMapping("/api/v1/tasks/{taskId}/subtasks")
    public ApiResponse<SubtaskResponse> createSubtask(
            @PathVariable Long taskId,
            @Validated @RequestBody SubtaskCreateRequest request) {
        SubtaskResponse response = subtaskService.createSubtask(taskId, request);
        return ApiResponse.success(response);
    }

    @PutMapping("/api/v1/subtasks/{id}")
    public ApiResponse<SubtaskResponse> updateSubtask(
            @PathVariable Long id,
            @RequestBody SubtaskUpdateRequest request) {
        SubtaskResponse response = subtaskService.updateSubtask(id, request);
        return ApiResponse.success(response);
    }

    @DeleteMapping("/api/v1/subtasks/{id}")
    public ApiResponse<Void> deleteSubtask(@PathVariable Long id) {
        subtaskService.deleteSubtask(id);
        return ApiResponse.success();
    }

    @PutMapping("/api/v1/subtasks/{id}/toggle")
    public ApiResponse<SubtaskResponse> toggleSubtask(@PathVariable Long id) {
        SubtaskResponse response = subtaskService.toggleSubtask(id);
        return ApiResponse.success(response);
    }

    @PutMapping("/api/v1/subtasks/reorder")
    public ApiResponse<Void> reorderSubtasks(@Validated @RequestBody SubtaskReorderRequest request) {
        subtaskService.reorderSubtasks(request);
        return ApiResponse.success();
    }
}
