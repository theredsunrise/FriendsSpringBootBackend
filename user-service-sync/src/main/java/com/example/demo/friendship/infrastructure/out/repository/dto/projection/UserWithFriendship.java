package com.example.demo.friendship.infrastructure.out.repository.dto.projection;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.bson.types.ObjectId;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UserWithFriendship implements UserProjectionWithFriendshipProjection {
    @EqualsAndHashCode.Include
    private ObjectId id;
    private UUID uuid;
    private String name;
    private String surname;
    private String username;
    private LocalDate birthDate;
    private String residence;
    private Instant createdAt;
    private Instant friendshipCreatedAt;
}