package com.example.demo.friendship.infrastructure.out.repository.jpa.dto.projection;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class UserWithFriendship implements UserProjectionWithFriendshipProjection {
    private Long id;
    private String name;
    private String surname;
    private String username;
    private LocalDate birthDate;
    private String residence;
    private Instant createdAt;
    private Instant friendshipCreatedAt;
}
