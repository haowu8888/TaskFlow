package com.taskflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("board_column")
public class BoardColumn {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("board_id")
    private Long boardId;

    @TableField("name")
    private String name;

    @TableField("color")
    private String color;

    @TableField("position")
    private Integer position;

    @TableField("wip_limit")
    private Integer wipLimit;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
