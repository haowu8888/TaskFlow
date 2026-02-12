package com.taskflow.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MemberInviteRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Role is required")
    private String role;
}
