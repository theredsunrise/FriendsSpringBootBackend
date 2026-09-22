package com.example.demo.user.domain;

import com.example.demo.user.application.exception.UserException;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class User implements Serializable {

    @EqualsAndHashCode.Include
    private UUID id;
    private String name;
    private String surname;
    private String username;
    private LocalDate birthDate;
    private String residence;
    private Instant createdAt;

    private User(UUID id,
                 String name,
                 String surname,
                 String username,
                 LocalDate birthDate,
                 String residence,
                 Instant createdAt) {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (name == null || name.isBlank()) {
            throw new UserException("Name cannot be null or empty.");
        }
        if (surname == null || surname.isBlank()) {
            throw new UserException("Surname cannot be null or empty.");
        }
        if (username == null || username.isBlank()) {
            throw new UserException("Username cannot be null or empty.");
        }
        if (birthDate == null) {
            throw new UserException("Birth date cannot be null.");
        }
        if (residence == null || residence.isBlank()) {
            throw new UserException("Residence cannot be null or empty.");
        }

        this.id = id;
        this.name = name;
        this.surname = surname;
        this.username = username;
        this.birthDate = birthDate;
        this.residence = residence;
        this.createdAt = createdAt;
    }
}

