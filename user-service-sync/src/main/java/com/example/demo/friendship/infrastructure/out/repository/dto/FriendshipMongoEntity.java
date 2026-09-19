package com.example.demo.friendship.infrastructure.out.repository.dto;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.UUID;

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

    private UUID uuid;

    @Field("id_user")
    private UUID userId;

    @Field("id_friend")
    private UUID friendId;

    @Field("created_at")
    private Instant createdAt;
}