package com.taskflow.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SubtaskCreateRequest {

    @NotBlank(message = "Subtask title is required")
    @Size(max = 200, message = "Title must be at most 200 characters")
    private String title;
}
