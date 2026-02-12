package com.taskflow.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class BoardColumnReorderRequest {

    @NotNull(message = "Column order items are required")
    private List<ColumnOrderItem> items;

    @Data
    public static class ColumnOrderItem {
        @NotNull(message = "Column ID is required")
        private Long id;

        @NotNull(message = "Position is required")
        private Integer position;
    }
}
