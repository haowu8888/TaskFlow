package com.taskflow.service;

import com.taskflow.dto.response.TaskActivityResponse;

import java.util.List;

public interface TaskActivityService {

    void logActivity(Long taskId, Long userId, String action, String fieldName, String oldValue, String newValue);

    List<TaskActivityResponse> getTaskActivities(Long taskId);
}
