package com.taskflow.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TaskMoveRequest {

    @NotNull(message = "Board column ID is required")
    private Long boardColumnId;

    private Integer position;
}
