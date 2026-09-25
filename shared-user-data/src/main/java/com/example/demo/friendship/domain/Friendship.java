package com.example.demo.friendship.domain;

import com.example.demo.friendship.application.exception.FriendshipWithSelfException;
import com.example.demo.user.application.exception.UserException;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;

@Getter
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Friendship implements Serializable {

    private Long id;
    private Long userId;
    private Long friendId;
    private Instant createdAt;

    private Friendship(Long id, Long userId, Long friendId, Instant createdAt) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Friendship that = (Friendship) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
