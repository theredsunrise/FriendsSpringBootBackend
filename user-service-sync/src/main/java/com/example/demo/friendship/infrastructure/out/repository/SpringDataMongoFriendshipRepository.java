package com.example.demo.friendship.infrastructure.out.repository;

import com.example.demo.friendship.infrastructure.out.repository.dto.FriendshipMongoEntity;
import com.example.demo.friendship.infrastructure.out.repository.dto.projection.UserWithFriendship;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface SpringDataMongoFriendshipRepository extends MongoRepository<FriendshipMongoEntity, String> {

    void deleteByUserIdAndFriendId(Long userId, Long friendId);

    boolean existsByUserIdAndFriendId(Long userId, Long friendId);

    @Aggregation(pipeline = {
            """
                    {
                      "$match": {
                        "$or": [
                          { "created_at": { "$lt": ?1 } },
                          {
                            "created_at": ?1,
                            "friendship_id": { "$lt": ?0 }
                          }
                        ]
                      }
                    }
                    """,
            """
                    {
                      "$sort": {
                        "created_at": -1,
                        "friendship_id": -1
                      }
                    }
                    """
    })
    Slice<FriendshipMongoEntity> findAll(
            @Param("lastId") Long lastId,
            @Param("lastCreatedAt") Instant lastCreatedAt,
            Pageable pageable
    );

    @Aggregation(pipeline = {
            """
                    {
                      "$match": {
                        "user_id": ?0,
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
                        "foreignField": "user_id",
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
                        "userId": "$friend.user_id",
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
            Long userId,
            Long lastFriendId,
            Instant lastCreatedAt,
            Pageable pageable
    );
}
