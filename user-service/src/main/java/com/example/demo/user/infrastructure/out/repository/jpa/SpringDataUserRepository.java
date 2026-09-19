package com.example.demo.user.infrastructure.out.repository.jpa;

import com.example.demo.user.infrastructure.out.repository.jpa.dto.UserJpaEntity;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;


@Repository
public interface SpringDataUserRepository
        extends JpaRepository<UserJpaEntity, UUID> {

    boolean existsByUsername(@NonNull String username);

    boolean existsById(@NonNull UUID userId);

    Window<UserJpaEntity> findByOrderByCreatedAtDescIdAsc(ScrollPosition scrollPosition, Limit limit);
}
