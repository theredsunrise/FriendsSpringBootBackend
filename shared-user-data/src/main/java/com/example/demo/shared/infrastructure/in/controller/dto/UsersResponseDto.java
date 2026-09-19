package com.example.demo.shared.infrastructure.in.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record UsersResponseDto(
        @Schema(description = "Users returned for current page")
        List<UserResponseDto> users,
        @Schema(description = "Current page information")
        PageDto currentPage,
        @Schema(description = "Next page information. Null when there is no next page")
        PageDto nextPage
) {
}
