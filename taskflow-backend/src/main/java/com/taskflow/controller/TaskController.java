package com.taskflow.controller;

import com.taskflow.dto.request.TaskCreateRequest;
import com.taskflow.dto.request.TaskMoveRequest;
import com.taskflow.dto.request.TaskStatusUpdateRequest;
import com.taskflow.dto.request.TaskUpdateRequest;
import com.taskflow.dto.response.ApiResponse;
import com.taskflow.dto.response.TaskListResponse;
import com.taskflow.dto.response.TaskResponse;
import com.taskflow.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping("/api/v1/workspaces/{workspaceId}/tasks")
    public ApiResponse<TaskResponse> createTask(
            @PathVariable Long workspaceId,
            @Validated @RequestBody TaskCreateRequest request) {
        Long creatorId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        TaskResponse response = taskService.createTask(workspaceId, request, creatorId);
        return ApiResponse.success(response);
    }

    @GetMapping("/api/v1/workspaces/{workspaceId}/tasks")
    public ApiResponse<TaskListResponse> listTasks(
            @PathVariable Long workspaceId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        TaskListResponse response = taskService.listTasks(workspaceId, status, priority, assigneeId, keyword, page, size, sortBy, sortDir);
        return ApiResponse.success(response);
    }

    @GetMapping("/api/v1/tasks/{id}")
    public ApiResponse<TaskResponse> getTask(@PathVariable Long id) {
        TaskResponse response = taskService.getTask(id);
        return ApiResponse.success(response);
    }

    @PutMapping("/api/v1/tasks/{id}")
    public ApiResponse<TaskResponse> updateTask(
            @PathVariable Long id,
            @RequestBody TaskUpdateRequest request) {
        TaskResponse response = taskService.updateTask(id, request);
        return ApiResponse.success(response);
    }

    @DeleteMapping("/api/v1/tasks/{id}")
    public ApiResponse<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ApiResponse.success();
    }

    @PutMapping("/api/v1/tasks/{id}/status")
    public ApiResponse<TaskResponse> updateStatus(
            @PathVariable Long id,
            @Validated @RequestBody TaskStatusUpdateRequest request) {
        TaskResponse response = taskService.updateStatus(id, request);
        return ApiResponse.success(response);
    }

    @PutMapping("/api/v1/tasks/{id}/move")
    public ApiResponse<TaskResponse> moveTask(
            @PathVariable Long id,
            @Validated @RequestBody TaskMoveRequest request) {
        TaskResponse response = taskService.moveTask(id, request);
        return ApiResponse.success(response);
    }

    @GetMapping("/api/v1/workspaces/{workspaceId}/tasks/calendar")
    public ApiResponse<List<TaskResponse>> getCalendarTasks(
            @PathVariable Long workspaceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        List<TaskResponse> response = taskService.getCalendarTasks(workspaceId, start, end);
        return ApiResponse.success(response);
    }

    @GetMapping("/api/v1/workspaces/{workspaceId}/tasks/gantt")
    public ApiResponse<List<TaskResponse>> getGanttTasks(@PathVariable Long workspaceId) {
        List<TaskResponse> response = taskService.getGanttTasks(workspaceId);
        return ApiResponse.success(response);
    }
}
