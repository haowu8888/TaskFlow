package com.taskflow.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TaskCreateRequest {

    @NotBlank(message = "Task title is required")
    @Size(max = 200, message = "Title must be at most 200 characters")
    private String title;

    private String description;

    private String priority;

    private String status;

    private LocalDate startDate;

    private LocalDate dueDate;

    private Long assigneeId;

    private Long parentTaskId;

    private Long boardColumnId;
}
