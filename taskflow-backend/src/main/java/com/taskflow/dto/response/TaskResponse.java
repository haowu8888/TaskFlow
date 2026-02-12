package com.taskflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {

    private Long id;
    private Long workspaceId;
    private Long boardColumnId;
    private String title;
    private String description;
    private String priority;
    private String status;
    private LocalDate startDate;
    private LocalDate dueDate;
    private Integer progress;
    private Long assigneeId;
    private Long creatorId;
    private Long parentTaskId;
    private Integer position;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private UserResponse assignee;
    private UserResponse creator;
    private List<SubtaskResponse> subtasks;
    private Integer commentCount;
}
