package com.taskflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("task_activity")
public class TaskActivity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("task_id")
    private Long taskId;

    @TableField("user_id")
    private Long userId;

    @TableField("action")
    private String action;

    @TableField("field_name")
    private String fieldName;

    @TableField("old_value")
    private String oldValue;

    @TableField("new_value")
    private String newValue;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
