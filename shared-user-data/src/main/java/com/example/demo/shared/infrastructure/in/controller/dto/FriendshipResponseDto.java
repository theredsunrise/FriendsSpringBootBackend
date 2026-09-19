package com.example.demo.shared.infrastructure.in.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

public record FriendshipResponseDto(
        @Schema(description = "Unique identifier of the friendship", example = "3fa85f64-5717-4562-b3fc-2c963f66afa8")
        UUID id,

        @Schema(description = "ID of the user", example = "3fa85f64-5717-4562-b3fc-2c963f65aaa1")
        UUID userId,

        @Schema(description = "ID of the friend", example = "3fa85f64-5010-4562-a3fc-2c963f66afa5")
        UUID friendId,

        @Schema(description = "Timestamp when the friendship was created")
        Instant createdAt
) {
}
