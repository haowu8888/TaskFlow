package com.taskflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoardResponse {

    private Long id;
    private Long workspaceId;
    private String name;
    private String description;
    private List<BoardColumnResponse> columns;
}
