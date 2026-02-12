package com.taskflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("refresh_token")
public class RefreshToken {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("token")
    private String token;

    @TableField("expires_at")
    private LocalDateTime expiresAt;

    @TableField("is_revoked")
    private Integer isRevoked;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
