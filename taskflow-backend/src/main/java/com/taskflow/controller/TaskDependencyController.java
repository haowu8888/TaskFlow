package com.taskflow.controller;

import com.taskflow.dto.request.TaskDependencyCreateRequest;
import com.taskflow.dto.response.ApiResponse;
import com.taskflow.dto.response.TaskResponse;
import com.taskflow.service.TaskDependencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TaskDependencyController {

    private final TaskDependencyService taskDependencyService;

    @PostMapping("/api/v1/tasks/{taskId}/dependencies")
    public ApiResponse<Void> addDependency(
            @PathVariable Long taskId,
            @Validated @RequestBody TaskDependencyCreateRequest request) {
        taskDependencyService.addDependency(taskId, request);
        return ApiResponse.success();
    }

    @DeleteMapping("/api/v1/tasks/{taskId}/dependencies")
    public ApiResponse<Void> removeDependency(
            @PathVariable Long taskId,
            @RequestParam Long predecessorId,
            @RequestParam Long successorId) {
        taskDependencyService.removeDependency(taskId, predecessorId, successorId);
        return ApiResponse.success();
    }

    @GetMapping("/api/v1/tasks/{taskId}/dependencies")
    public ApiResponse<List<TaskResponse>> getDependencies(@PathVariable Long taskId) {
        List<TaskResponse> response = taskDependencyService.getDependencies(taskId);
        return ApiResponse.success(response);
    }
}
