package com.example.demo.shared.infrastructure.out.repository.migration;


import io.flamingock.api.annotations.Apply;
import io.flamingock.api.annotations.Change;
import io.flamingock.api.annotations.Rollback;
import io.flamingock.api.annotations.TargetSystem;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;

import static com.example.demo.shared.infrastructure.out.repository.mongo.FlamingockConfig.MONGO_SCHEMA_ID;
import static com.example.demo.shared.infrastructure.out.repository.mongo.FlamingockConfig.MONGO_TARGET_SYSTEM_ID;

@TargetSystem(id = MONGO_TARGET_SYSTEM_ID)
@Change(id = MONGO_SCHEMA_ID, author = "developer", transactional = false)
public class _0001__init_schema {

    private static final String USERS_COLLECTION_NAME = "users";
    private static final String USERS_USERNAME_INDEX_NAME = "idx_users_username";
    private static final String USERS_ID_INDEX_NAME = "idx_users_id";
    private static final String USERS_CREATED_AT_INDEX_NAME = "idx_users_created_at";

    private static final String FRIENDSHIP_COLLECTION_NAME = "friendships";
    private static final String FRIENDSHIP_USER_INDEX_NAME = "idx_friendships_user_id";
    private static final String FRIENDSHIP_FRIEND_INDEX_NAME = "idx_friendships_friend_id";
    private static final String FRIENDSHIP_CREATED_AT_INDEX_NAME = "idx_friendships_created_at";

    @Apply
    public void apply(MongoTemplate mongoTemplate) {
        prepareUsers(mongoTemplate);
        prepareFriendship(mongoTemplate);
    }

    private void prepareUsers(MongoTemplate mongoTemplate) {
        if (!mongoTemplate.collectionExists(USERS_COLLECTION_NAME)) {
            mongoTemplate.createCollection(USERS_COLLECTION_NAME);
        }

        Index usernameIndex = new Index()
                .on("username", Sort.Direction.ASC)
                .named(USERS_USERNAME_INDEX_NAME)
                .unique();

        Index idIndex = new Index()
                .on("user_id", Sort.Direction.ASC)
                .named(USERS_ID_INDEX_NAME);

        Index createdAtIndex = new Index()
                .on("created_at", Sort.Direction.DESC)
                .named(USERS_CREATED_AT_INDEX_NAME);

        IndexOperations indexOps = mongoTemplate.indexOps(USERS_COLLECTION_NAME);
        indexOps.createIndex(usernameIndex);
        indexOps.createIndex(idIndex);
        indexOps.createIndex(createdAtIndex);
    }

    private void prepareFriendship(MongoTemplate mongoTemplate) {
        if (!mongoTemplate.collectionExists(FRIENDSHIP_COLLECTION_NAME)) {
            mongoTemplate.createCollection(FRIENDSHIP_COLLECTION_NAME);
        }

        Index userIndex = new Index()
                .on("user_id", Sort.Direction.ASC)
                .named(FRIENDSHIP_USER_INDEX_NAME);


        Index friendIndex = new Index()
                .on("id_friend", Sort.Direction.DESC)
                .named(FRIENDSHIP_FRIEND_INDEX_NAME);

        Index createdAtIndex = new Index()
                .on("created_at", Sort.Direction.DESC)
                .named(FRIENDSHIP_CREATED_AT_INDEX_NAME);

        IndexOperations indexOps = mongoTemplate.indexOps(FRIENDSHIP_COLLECTION_NAME);
        indexOps.createIndex(new Index()
                .on("friendship_id", Sort.Direction.DESC)
                .named("idx_friendships_id"));
        indexOps.createIndex(userIndex);
        indexOps.createIndex(friendIndex);
        indexOps.createIndex(createdAtIndex);
    }

    @Rollback
    public void rollback(MongoTemplate mongoTemplate) {

        if (mongoTemplate.collectionExists(USERS_COLLECTION_NAME)) {
            mongoTemplate.dropCollection(USERS_COLLECTION_NAME);
        }

        if (mongoTemplate.collectionExists(FRIENDSHIP_COLLECTION_NAME)) {
            mongoTemplate.dropCollection(FRIENDSHIP_COLLECTION_NAME);
        }
    }
}
