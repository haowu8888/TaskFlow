package com.taskflow.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommentUpdateRequest {

    @NotBlank(message = "Comment content is required")
    private String content;
}
