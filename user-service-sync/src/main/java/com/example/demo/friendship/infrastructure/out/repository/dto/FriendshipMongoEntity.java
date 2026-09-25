package com.example.demo.friendship.infrastructure.out.repository.dto;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

@Document(collection = "friendships")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendshipMongoEntity {

    @Id
    private ObjectId id;

    @Field("friendship_id")
    private Long friendshipId;

    @Field("user_id")
    private Long userId;

    @Field("id_friend")
    private Long friendId;

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
        FriendshipMongoEntity that = (FriendshipMongoEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
