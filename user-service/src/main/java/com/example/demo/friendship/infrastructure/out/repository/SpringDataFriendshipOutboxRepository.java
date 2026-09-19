package com.example.demo.friendship.infrastructure.out.repository;

import com.example.demo.shared.domain.OutBoxStatus;
import com.example.demo.friendship.infrastructure.out.repository.jpa.dto.FriendshipOutboxJpaEntity;
import io.micrometer.observation.annotation.Observed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SpringDataFriendshipOutboxRepository extends JpaRepository<FriendshipOutboxJpaEntity, Long> {

    int deleteByEventStatus(OutBoxStatus status);

    @Modifying
    @Query("""
            UPDATE FriendshipOutboxJpaEntity f
            SET f.eventStatus = :status
            WHERE f.id = :id
            """)
    int updateStatus(
            @Param("id") Long id,
            @Param("status") OutBoxStatus status
    );
}
