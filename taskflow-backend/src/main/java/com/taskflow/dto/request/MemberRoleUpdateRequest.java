package com.taskflow.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MemberRoleUpdateRequest {

    @NotBlank(message = "Role is required")
    private String role;
}
