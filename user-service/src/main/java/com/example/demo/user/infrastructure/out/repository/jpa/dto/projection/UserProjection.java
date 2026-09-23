package com.example.demo.user.infrastructure.out.repository.jpa.dto.projection;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.Instant;
import java.time.LocalDate;

public interface UserProjection {

     Long getId();

     String getName();

     String getSurname();

     String getUsername();

     LocalDate getBirthDate();

     String getResidence();

     Instant getCreatedAt();
}
