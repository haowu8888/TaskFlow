package com.taskflow.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BoardCreateRequest {

    @NotBlank(message = "Board name is required")
    @Size(max = 100, message = "Name must be at most 100 characters")
    private String name;

    private String description;
}
