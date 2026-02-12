package com.taskflow.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class TaskUpdateRequest {

    private String title;

    private String description;

    private String priority;

    private String status;

    private LocalDate startDate;

    private LocalDate dueDate;

    private Long assigneeId;

    private Long parentTaskId;

    private Long boardColumnId;

    private Integer progress;
}
