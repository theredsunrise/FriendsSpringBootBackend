package com.example.demo.friendship.infrastructure.out.repository.dto.projection;

import com.example.demo.user.infrastructure.out.repository.dto.projection.UserProjection;

import java.time.Instant;

public interface UserProjectionWithFriendshipProjection extends UserProjection {
    Instant getFriendshipCreatedAt();
}
