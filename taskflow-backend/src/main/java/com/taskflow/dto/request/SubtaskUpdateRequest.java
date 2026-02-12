package com.taskflow.dto.request;

import lombok.Data;

@Data
public class SubtaskUpdateRequest {

    private String title;

    private Integer isCompleted;
}
