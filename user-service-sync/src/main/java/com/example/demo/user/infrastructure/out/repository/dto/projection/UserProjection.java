package com.example.demo.user.infrastructure.out.repository.dto.projection;

import org.bson.types.ObjectId;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public interface UserProjection {

     ObjectId getId();

     UUID getUuid();

     String getName();

     String getSurname();

     String getUsername();

     LocalDate getBirthDate();

     String getResidence();

     Instant getCreatedAt();
}
