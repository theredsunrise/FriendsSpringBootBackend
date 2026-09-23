package com.example.demo.friendship.infrastructure.in.event.dto;



public record FriendshipEventResponse(
        Long id,
        Long userId,
        Long friendId,
        boolean result){}
