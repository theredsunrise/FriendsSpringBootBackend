package com.example.demo.friendship.infrastructure.out.repository.dto.projection;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UserWithFriendship implements UserProjectionWithFriendshipProjection {
    @EqualsAndHashCode.Include
    @NonNull
    private ObjectId id;
    @NonNull
    @Field("user_id")
    private Long userId;
    @NonNull
    private String name;
    @NonNull
    private String surname;
    @NonNull
    private String username;
    @NonNull
    private LocalDate birthDate;
    @NonNull
    private String residence;
    @NonNull
    @Field("created_at")
    private Instant createdAt;
    @NonNull
    private Instant friendshipCreatedAt;
}
