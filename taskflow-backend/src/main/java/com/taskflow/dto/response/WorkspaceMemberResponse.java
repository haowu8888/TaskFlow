package com.taskflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceMemberResponse {

    private Long id;
    private Long userId;
    private String username;
    private String email;
    private String avatarUrl;
    private String role;
    private LocalDateTime joinedAt;
}
