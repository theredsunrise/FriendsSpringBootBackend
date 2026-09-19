package com.example.demo.user.infrastructure.out.repository.jpa;

import com.example.demo.shared.domain.OutBoxStatus;
import com.example.demo.user.infrastructure.out.repository.jpa.dto.UserOutboxJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataUserOutboxRepository extends JpaRepository<UserOutboxJpaEntity, Long> {

    int deleteByEventStatus(OutBoxStatus status);

    @Modifying
    @Query("""
            UPDATE UserOutboxJpaEntity u
            SET u.eventStatus = :status
            WHERE u.id = :id
            """)
    int updateStatus(
            @Param("id") Long id,
            @Param("status") OutBoxStatus status
    );
}
