package com.example.demo.friendship.infrastructure.in.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddFriendToUserDto(
        @NotNull(message = "User id is required")
        @Schema(
                description = "ID of the user",
                example = "550e8400-e29b-41d4-a716-446655440000"
        )
        UUID userId,


        @NotNull(message = "Friend id is required")
        @Schema(
                description = "ID of the friend",
                example = "6ba7b810-9dad-11d1-80b4-00c04fd430c8"
        )
        UUID friendId
){}
