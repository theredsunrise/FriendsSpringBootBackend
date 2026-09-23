package com.example.demo.friendship.infrastructure.in.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;


public record AddFriendToUserDto(
        @NotNull(message = "User id is required")
        @Schema(
                description = "Numeric identifier of the user",
                example = "101"
        )
        Long userId,


        @NotNull(message = "Friend id is required")
        @Schema(
                description = "Numeric identifier of the friend",
                example = "202"
        )
        Long friendId
){}
