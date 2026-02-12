package com.taskflow.dto.request;

import lombok.Data;

@Data
public class BoardColumnUpdateRequest {

    private String name;

    private String color;

    private Integer wipLimit;
}
