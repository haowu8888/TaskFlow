package com.taskflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceResponse {

    private Long id;
    private String name;
    private String description;
    private UserResponse owner;
    private Integer memberCount;
    private LocalDateTime createdAt;
}
