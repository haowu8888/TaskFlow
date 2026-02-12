package com.taskflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("workspace_member")
public class WorkspaceMember {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("workspace_id")
    private Long workspaceId;

    @TableField("user_id")
    private Long userId;

    @TableField("role")
    private String role;

    @TableField("joined_at")
    private LocalDateTime joinedAt;
}
