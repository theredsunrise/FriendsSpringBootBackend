package com.example.demo.friendship.infrastructure.out.repository;

import com.example.demo.friendship.infrastructure.out.repository.jpa.dto.FriendshipJpaEntity;
import com.example.demo.friendship.infrastructure.out.repository.jpa.dto.projection.UserWithFriendship;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface SpringDataFriendshipRepository
        extends JpaRepository<FriendshipJpaEntity, Long> {

    void deleteByUserIdAndFriendId(Long userId, Long friendId);

    boolean existsByUserIdAndFriendId(Long userId, Long friendId);

    Window<FriendshipJpaEntity> findAllByOrderByCreatedAtDescIdDesc(ScrollPosition scrollPosition, Limit limit);

    @Query("""
            SELECT
             u.id,
             u.name,
             u.surname,
             u.username,
             u.birthDate,
             u.residence,
             u.createdAt,
             f.createdAt as friendshipCreatedAt
            FROM UserJpaEntity u
            JOIN FriendshipJpaEntity f ON u.id = f.friendId
            WHERE f.userId = :userId AND (
                        f.createdAt < :lastCreatedAt OR
                        (f.createdAt = :lastCreatedAt AND f.friendId < :lastFriendId)
                        )
            ORDER BY f.createdAt DESC, f.friendId DESC
            """)
    Slice<UserWithFriendship> findAllFriends(
            @Param("userId") Long userId,
            @Param("lastFriendId") Long lastFriendId,
            @Param("lastCreatedAt") Instant lastCreatedAt,
            Pageable pageable
    );
}
