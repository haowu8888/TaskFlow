package com.taskflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubtaskResponse {

    private Long id;
    private Long taskId;
    private String title;
    private Integer isCompleted;
    private Integer position;
}
