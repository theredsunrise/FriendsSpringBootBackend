package com.example.demo.user.infrastructure.out.repository.dto.projection;

import org.bson.types.ObjectId;

import java.time.Instant;
import java.time.LocalDate;

public interface UserProjection {

     ObjectId getId();

     Long getUserId();

     String getName();

     String getSurname();

     String getUsername();

     LocalDate getBirthDate();

     String getResidence();

     Instant getCreatedAt();
}
