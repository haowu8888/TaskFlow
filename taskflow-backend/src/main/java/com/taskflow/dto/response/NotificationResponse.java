package com.taskflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private Long id;
    private String type;
    private String title;
    private String content;
    private Long referenceId;
    private Integer isRead;
    private LocalDateTime createdAt;
}
