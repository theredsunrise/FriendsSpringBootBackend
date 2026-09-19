package com.example.demo.user.infrastructure.out.repository.jpa.dto;

import com.example.demo.user.infrastructure.out.repository.jpa.dto.projection.UserProjection;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;


@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UserJpaEntity implements UserProjection {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String surname;


    @Column(nullable = false, length = 50, unique = true)
    private String username;


    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;


    @Column(nullable = false, length = 150)
    private String residence;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
