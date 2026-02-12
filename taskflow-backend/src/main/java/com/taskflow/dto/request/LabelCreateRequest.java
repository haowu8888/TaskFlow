package com.taskflow.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LabelCreateRequest {

    @NotBlank(message = "Label name is required")
    private String name;

    private String color = "#165DFF";
}
