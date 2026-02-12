package com.taskflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoardColumnResponse {

    private Long id;
    private Long boardId;
    private String name;
    private String color;
    private Integer position;
    private Integer wipLimit;
    private List<TaskResponse> tasks;
}
