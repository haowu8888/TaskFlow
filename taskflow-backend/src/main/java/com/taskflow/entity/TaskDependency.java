package com.taskflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("task_dependency")
public class TaskDependency {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("predecessor_task_id")
    private Long predecessorTaskId;

    @TableField("successor_task_id")
    private Long successorTaskId;

    @TableField("dependency_type")
    private String dependencyType;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
