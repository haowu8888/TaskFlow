package com.taskflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LabelResponse {

    private Long id;
    private Long workspaceId;
    private String name;
    private String color;
    private LocalDateTime createdAt;
}
