package com.example.demo.user.infrastructure.out.repository.dto;

import com.example.demo.user.infrastructure.out.repository.dto.projection.UserProjection;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Document(collection = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UserMongoEntity implements UserProjection {
    @Id
    @EqualsAndHashCode.Include
    private ObjectId id;

    private UUID uuid;

    private String name;

    private String surname;

    private String username;

    @Field("birth_date")
    private LocalDate birthDate;

    private String residence;

    @Field("created_at")
    private Instant createdAt;
}