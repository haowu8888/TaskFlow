package com.taskflow.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SubtaskReorderRequest {

    @NotNull(message = "Subtask order items are required")
    private List<SubtaskOrderItem> items;

    @Data
    public static class SubtaskOrderItem {
        @NotNull(message = "Subtask ID is required")
        private Long id;

        @NotNull(message = "Position is required")
        private Integer position;
    }
}
