package com.taskflow.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BoardColumnCreateRequest {

    @NotBlank(message = "Column name is required")
    @Size(max = 50, message = "Name must be at most 50 characters")
    private String name;

    private String color;

    private Integer wipLimit;
}
