package com.example.demo.user.infrastructure.in.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UserCreateDto(

        @Schema(
                description = "Name of the user",
                example = "John"
        )
        @NotBlank(message = "Name must not be blank")
        @Size(min = 3, max = 100, message = "Name must be between 5 and 100 characters")
        String name,


        @Schema(
                description = "Surname of the user",
                example = "Doe"
        )
        @NotBlank(message = "Surname must not be blank")
        @Size(min = 3, max = 100, message = "Surname must be between 5 and 100 characters")
        String surname,


        @Schema(
                description = "Unique username",
                example = "john.doe"
        )
        @NotBlank(message = "Username must not be blank")
        @Size(min = 3, max = 50, message = "Username must be between 5 and 50 characters")
        String username,


        @Schema(
                description = "Date of birth",
                example = "1990-05-20"
        )
        @NotNull(message = "Birth date must not be null")
        @Past(message = "Birth date must be in the past")
        LocalDate birthDate,


        @Schema(
                description = "Place of residence",
                example = "Bratislava"
        )
        @NotBlank(message = "Residence must not be blank")
        @Size(min = 3, max = 150, message = "Residence must be between 5 and 150 characters")
        String residence

) {
}