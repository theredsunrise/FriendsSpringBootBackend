package com.example.demo.user.infrastructure.out.repository.jpa.dto.projection;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public interface UserProjection {

     UUID getId();

     String getName();

     String getSurname();

     String getUsername();

     LocalDate getBirthDate();

     String getResidence();

     Instant getCreatedAt();
}
