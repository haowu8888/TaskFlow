package com.taskflow.service;

import com.taskflow.dto.request.TaskDependencyCreateRequest;
import com.taskflow.dto.response.TaskResponse;

import java.util.List;

public interface TaskDependencyService {

    void addDependency(Long taskId, TaskDependencyCreateRequest request);

    void removeDependency(Long taskId, Long predecessorId, Long successorId);

    List<TaskResponse> getDependencies(Long taskId);
}
