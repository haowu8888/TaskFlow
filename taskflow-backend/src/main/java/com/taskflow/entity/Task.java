package com.taskflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("task")
public class Task {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("workspace_id")
    private Long workspaceId;

    @TableField("board_column_id")
    private Long boardColumnId;

    @TableField("title")
    private String title;

    @TableField("description")
    private String description;

    @TableField("priority")
    private String priority;

    @TableField("status")
    private String status;

    @TableField("start_date")
    private LocalDate startDate;

    @TableField("due_date")
    private LocalDate dueDate;

    @TableField("progress")
    private Integer progress;

    @TableField("assignee_id")
    private Long assigneeId;

    @TableField("creator_id")
    private Long creatorId;

    @TableField("parent_task_id")
    private Long parentTaskId;

    @TableField("position")
    private Integer position;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
