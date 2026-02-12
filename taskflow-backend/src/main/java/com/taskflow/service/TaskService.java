package com.taskflow.service;

import com.taskflow.dto.request.TaskCreateRequest;
import com.taskflow.dto.request.TaskMoveRequest;
import com.taskflow.dto.request.TaskStatusUpdateRequest;
import com.taskflow.dto.request.TaskUpdateRequest;
import com.taskflow.dto.response.TaskListResponse;
import com.taskflow.dto.response.TaskResponse;

import java.time.LocalDate;
import java.util.List;

public interface TaskService {

    TaskResponse createTask(Long workspaceId, TaskCreateRequest request, Long creatorId);

    TaskResponse getTask(Long id);

    TaskResponse updateTask(Long id, TaskUpdateRequest request);

    void deleteTask(Long id);

    TaskListResponse listTasks(Long workspaceId, String status, String priority, Long assigneeId,
                               String keyword, int page, int size, String sortBy, String sortDir);

    TaskResponse updateStatus(Long id, TaskStatusUpdateRequest request);

    TaskResponse moveTask(Long id, TaskMoveRequest request);

    List<TaskResponse> getCalendarTasks(Long workspaceId, LocalDate start, LocalDate end);

    List<TaskResponse> getGanttTasks(Long workspaceId);
}
