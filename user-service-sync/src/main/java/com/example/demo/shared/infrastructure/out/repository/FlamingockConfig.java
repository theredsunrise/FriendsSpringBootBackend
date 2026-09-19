package com.example.demo.shared.infrastructure.out.repository;

import io.flamingock.store.mongodb.sync.MongoDBSyncAuditStore;
import io.flamingock.targetsystem.mongodb.springdata.MongoDBSpringDataTargetSystem;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class FlamingockConfig {

    public static final String MONGO_SCHEMA_ID = "mongodb-schema";
    public static final String MONGO_TARGET_SYSTEM_ID = "mongodb-target-system";
    public static final String MONGO_TARGET_SYSTEM = "mongoTargetSystem";
    public static final String AUDIT_STORE = "auditStore";

    @Bean(MONGO_TARGET_SYSTEM)
    public MongoDBSpringDataTargetSystem mongoTargetSystem(
            MongoTemplate mongoTemplate
    ) {
        return new MongoDBSpringDataTargetSystem(
                MONGO_TARGET_SYSTEM_ID,
                mongoTemplate
        );
    }

    @Bean(AUDIT_STORE)
    public MongoDBSyncAuditStore auditStore(@Qualifier(MONGO_TARGET_SYSTEM) MongoDBSpringDataTargetSystem mongoDBSyncTargetSystem) {
        return MongoDBSyncAuditStore.from(mongoDBSyncTargetSystem);
    }
}
