package com.example.demo.user.infrastructure.out.repository;

import com.example.demo.user.infrastructure.out.repository.dto.UserMongoEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface SpringDataMongoUserRepository extends MongoRepository<UserMongoEntity, String> {

    boolean existsByUsername(String username);

    boolean existsByUserId(Long userId);

    void deleteByUserId(Long userId);

    Optional<UserMongoEntity> findByUserId(Long userId);

    @Query(
            value = """
        {
          "$or": [
            { "createdAt": { "$lt": ?1 } },
            {
              "createdAt": ?1,
              "user_id": { "$lt": ?0 }
            }
          ]
        }
        """,
            sort = """
        {
          "createdAt": -1,
          "user_id": -1
        }
        """
    )
    Slice<UserMongoEntity> findAll(
            @Param("lastUserId") Long lastUserId,
            @Param("lastCreatedAt") Instant lastCreatedAt,
            Pageable pageable
    );
}
