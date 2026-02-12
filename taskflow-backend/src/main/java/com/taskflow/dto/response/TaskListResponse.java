package com.taskflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskListResponse {

    private List<TaskResponse> records;
    private long total;
    private long size;
    private long current;
    private long pages;
}
