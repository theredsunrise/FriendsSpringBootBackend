package com.example.demo.shared.infrastructure.in.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

public record PageDto(
        @Schema(
                description = "Position token",
                nullable = true,
                defaultValue = "null"
        )
        String token,

        @Schema(
                description = "Number of items per page",
                example = "20",
                minimum = "1"
        )
        @Positive(message = "Size must be greater than 0")
        @Min(value = 5, message = "Size must be at least 4")
        int size
) {
}

