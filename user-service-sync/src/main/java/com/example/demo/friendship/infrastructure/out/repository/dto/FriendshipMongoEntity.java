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
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class FriendshipMongoEntity {

    @Id
    @EqualsAndHashCode.Include
    private ObjectId id;

    @Field("friendship_id")
    private Long friendshipId;

    @Field("user_id")
    private Long userId;

    @Field("id_friend")
    private Long friendId;

    @Field("created_at")
    private Instant createdAt;
}
