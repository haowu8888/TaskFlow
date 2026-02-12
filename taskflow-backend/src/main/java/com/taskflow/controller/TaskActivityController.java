package com.taskflow.controller;

import com.taskflow.dto.response.ApiResponse;
import com.taskflow.dto.response.TaskActivityResponse;
import com.taskflow.service.TaskActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TaskActivityController {

    private final TaskActivityService taskActivityService;

    @GetMapping("/api/v1/tasks/{taskId}/activities")
    public ApiResponse<List<TaskActivityResponse>> getTaskActivities(@PathVariable Long taskId) {
        List<TaskActivityResponse> response = taskActivityService.getTaskActivities(taskId);
        return ApiResponse.success(response);
    }
}
