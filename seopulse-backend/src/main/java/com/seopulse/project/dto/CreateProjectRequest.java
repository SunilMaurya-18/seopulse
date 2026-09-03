package com.seopulse.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateProjectRequest(
        @NotBlank(message = "Project name required")
        @Size(max = 150,
                message = "Project name must not exceed 150 characteres"
        )
        String name,
        @Size(max = 500,
                message = "Project description must not exceed 150 characteres"
        )
        String description
) {
}
