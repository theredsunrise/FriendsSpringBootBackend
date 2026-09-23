package com.example.demo.user.infrastructure.out.repository.jpa;

import com.example.demo.user.infrastructure.out.repository.jpa.dto.UserJpaEntity;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface SpringDataUserRepository
        extends JpaRepository<UserJpaEntity, Long> {

    boolean existsByUsername(@NonNull String username);

    boolean existsById(@NonNull Long userId);

    Window<UserJpaEntity> findByOrderByCreatedAtDescIdAsc(ScrollPosition scrollPosition, Limit limit);
}
