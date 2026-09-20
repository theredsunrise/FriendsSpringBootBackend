package com.example.demo.friendship.infrastructure.out.repository.jpa.dto;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "friendships")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class FriendshipJpaEntity {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "id_user", nullable = false)
    private UUID userId;

    @Column(name = "id_friend", nullable = false)
    private UUID friendId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
