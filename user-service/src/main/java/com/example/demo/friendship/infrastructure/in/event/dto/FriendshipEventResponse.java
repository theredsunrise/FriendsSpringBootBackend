package com.example.demo.friendship.infrastructure.in.event.dto;


import java.util.UUID;

public record FriendshipEventResponse(
        Long id,
        UUID userId,
        UUID friendId,
        boolean result){}

