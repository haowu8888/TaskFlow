package com.taskflow.dto.request;

import lombok.Data;

@Data
public class WorkspaceUpdateRequest {

    private String name;

    private String description;
}
