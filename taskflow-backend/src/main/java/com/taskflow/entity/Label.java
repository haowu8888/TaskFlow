package com.taskflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("label")
public class Label {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("workspace_id")
    private Long workspaceId;

    @TableField("name")
    private String name;

    @TableField("color")
    private String color;

    @TableField("created_by")
    private Long createdBy;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
