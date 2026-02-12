package com.taskflow.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TaskDependencyCreateRequest {

    @NotNull(message = "Predecessor task ID is required")
    private Long predecessorTaskId;

    private Long successorTaskId;

    private String dependencyType;
}
