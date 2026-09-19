package com.example.demo.shared.infrastructure.in.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record UserResponseDto(
        @Schema(description = "Unique identifier of the user", example = "3fa85f64-5717-4562-b3fc-2c963f66afa8")
        UUID id,
        @Schema(description = "Name of the user", example = "John")
        String name,
        @Schema(description = "Surname of the user", example = "Doe")
        String surname,
        @Schema(description = "Unique username of the user", example = "john.doe")
        String username,
        @Schema(description = "Date of birth of the user", example = "1990-05-20")
        LocalDate birthDate,
        @Schema(description = "Current residence of the user", example = "Bratislava")
        String residence,
        @Schema(description = "Timestamp when the user was created")
        Instant createdAt
) {
}
