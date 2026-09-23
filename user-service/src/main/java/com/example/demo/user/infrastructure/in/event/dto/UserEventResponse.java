package com.example.demo.user.infrastructure.in.event.dto;



public record UserEventResponse(
        Long id,
        Long userId,
        boolean result){}
