package com.taskflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("task_label")
public class TaskLabel {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("task_id")
    private Long taskId;

    @TableField("label_id")
    private Long labelId;
}
