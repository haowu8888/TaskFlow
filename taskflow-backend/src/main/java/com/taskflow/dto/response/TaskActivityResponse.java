package com.taskflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskActivityResponse {

    private Long id;
    private Long taskId;
    private String action;
    private String fieldName;
    private String oldValue;
    private String newValue;
    private LocalDateTime createdAt;
    private UserResponse user;
}
