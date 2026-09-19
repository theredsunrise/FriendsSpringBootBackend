package com.example.demo.user.infrastructure.out.repository;

import com.example.demo.user.infrastructure.out.repository.dto.UserMongoEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataMongoUserRepository extends MongoRepository<UserMongoEntity, String> {

    boolean existsByUsername(String username);

    boolean existsByUuid(UUID userId);

    void deleteByUuid(UUID userId);

    Optional<UserMongoEntity> findByUuid(UUID userId);

    @Query(
            value = """
        {
          "$or": [
            { "createdAt": { "$lt": ?1 } },
            {
              "createdAt": ?1,
              "uuid": { "$lt": ?0 }
            }
          ]
        }
        """,
            sort = """
        {
          "createdAt": -1,
          "uuid": -1
        }
        """
    )
    Slice<UserMongoEntity> findAll(
            @Param("lastUserId") UUID lastUserId,
            @Param("lastCreatedAt") Instant lastCreatedAt,
            Pageable pageable
    );
}

