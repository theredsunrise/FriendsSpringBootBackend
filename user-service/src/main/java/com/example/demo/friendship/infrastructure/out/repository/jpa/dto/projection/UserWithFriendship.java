package com.example.demo.friendship.infrastructure.out.repository.jpa.dto.projection;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class UserWithFriendship implements UserProjectionWithFriendshipProjection {
    private UUID id;
    private String name;
    private String surname;
    private String username;
    private LocalDate birthDate;
    private String residence;
    private Instant createdAt;
    private Instant friendshipCreatedAt;
}