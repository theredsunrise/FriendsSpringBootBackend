package com.example.demo.friendship.infrastructure.out.repository.jpa.dto.projection;

import com.example.demo.user.infrastructure.out.repository.jpa.dto.projection.UserProjection;

import java.time.Instant;

public interface UserProjectionWithFriendshipProjection extends UserProjection {
    Instant getFriendshipCreatedAt();
}
