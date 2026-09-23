package com.example.demo.shared.infrastructure.in.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

public record FriendshipResponseDto(
        @Schema(description = "Numeric identifier of the friendship", example = "1001")
        Long id,

        @Schema(description = "Numeric identifier of the user", example = "101")
        Long userId,

        @Schema(description = "Numeric identifier of the friend", example = "202")
        Long friendId,

        @Schema(description = "Timestamp when the friendship was created")
        Instant createdAt
) {
}
