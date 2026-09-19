package com.example.demo.friendship.domain;

import com.example.demo.friendship.application.exception.FriendshipWithSelfException;
import com.example.demo.user.application.exception.UserException;
import lombok.*;

import java.time.Instant;
import java.io.Serializable;
import java.util.UUID;

@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Friendship implements Serializable {

    @EqualsAndHashCode.Include
    private UUID id;
    private UUID userId;
    private UUID friendId;
    private Instant createdAt;

    private Friendship(UUID id, UUID userId, UUID friendId, Instant createdAt) {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (userId == null || friendId == null) {
            throw new UserException("User ID or Friend ID cannot be null.");
        }
        if (userId.equals(friendId)) {
            throw new FriendshipWithSelfException("A user with ID: %s cannot be friends with themselves.".formatted(userId));
        }

        this.id = id;
        this.userId = userId;
        this.friendId = friendId;
        this.createdAt = createdAt;
    }
}

