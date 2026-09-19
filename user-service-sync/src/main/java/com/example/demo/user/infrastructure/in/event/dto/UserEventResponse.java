package com.example.demo.user.infrastructure.in.event.dto;


import java.util.UUID;

public record UserEventResponse(
        Long id,
        UUID userId,
        boolean result){}

