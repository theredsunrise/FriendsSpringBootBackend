package com.example.demo.friendship.infrastructure.out.repository;

import com.example.demo.friendship.infrastructure.out.repository.dto.FriendshipMongoEntity;
import com.example.demo.friendship.infrastructure.out.repository.dto.projection.UserWithFriendship;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface SpringDataMongoFriendshipRepository extends MongoRepository<FriendshipMongoEntity, String> {

    void deleteByUserIdAndFriendId(UUID userId, UUID friendId);

    boolean existsByUserIdAndFriendId(UUID userId, UUID friendId);

    @Aggregation(pipeline = {
            """
                    {
                      "$match": {
                        "$or": [
                          { "createdAt": { "$lt": ?1 } },
                          {
                            "createdAt": ?1,
                            "uuid": { "$lt": ?0 }
                          }
                        ]
                      }
                    }
                    """,
            """
                    {
                      "$sort": {
                        "createdAt": -1,
                        "uuid": -1
                      }
                    }
                    """
    })
    Slice<FriendshipMongoEntity> findAll(
            @Param("lastUuid") UUID lastUuid,
            @Param("lastCreatedAt") Instant lastCreatedAt,
            Pageable pageable
    );

    @Aggregation(pipeline = {
            """
                    {
                      "$match": {
                        "id_user": ?0,
                        "$or": [
                          {
                            "created_at": {
                              "$lt": ?2
                            }
                          },
                          {
                            "created_at": ?2,
                            "id_friend": {
                              "$lt": ?1
                            }
                          }
                        ]
                      }
                    }
                    """,

            """
                    {
                      "$sort": {
                        "created_at": -1,
                        "id_friend": -1
                      }
                    }
                    """,

            """
                    {
                      "$lookup": {
                        "from": "users",
                        "localField": "id_friend",
                        "foreignField": "uuid",
                        "as": "friend"
                      }
                    }
                    """,

            """
                    {
                      "$unwind": {
                        "path": "$friend",
                        "preserveNullAndEmptyArrays": false
                      }
                    }
                    """,

            """
                    {
                      "$project": {
                        "_id": "$friend._id",
                        "uuid": "$friend.uuid",
                        "name": "$friend.name",
                        "surname": "$friend.surname",
                        "username": "$friend.username",
                        "birthDate": "$friend.birth_date",
                        "residence": "$friend.residence",
                        "createdAt": "$friend.created_at",
                        "friendshipCreatedAt": "$created_at"
                      }
                    }
                    """
    })
    Slice<UserWithFriendship> findAllFriends(
            UUID userId,
            UUID lastFriendId,
            Instant lastCreatedAt,
            Pageable pageable
    );
}

