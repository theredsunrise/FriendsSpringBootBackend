package com.example.demo.user.infrastructure.out.repository.dto;

import com.example.demo.user.infrastructure.out.repository.dto.projection.UserProjection;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.time.LocalDate;

@Document(collection = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMongoEntity implements UserProjection {
    @Id
    private ObjectId id;

    @Field("user_id")
    private Long userId;

    private String name;

    private String surname;

    private String username;

    @Field("birth_date")
    private LocalDate birthDate;

    private String residence;

    @Field("created_at")
    private Instant createdAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserMongoEntity that = (UserMongoEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
