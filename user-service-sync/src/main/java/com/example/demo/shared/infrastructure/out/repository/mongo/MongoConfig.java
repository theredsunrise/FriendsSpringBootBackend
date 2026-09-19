package com.example.demo.shared.infrastructure.out.repository.mongo;

import com.mongodb.observability.ObservabilitySettings;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.boot.mongodb.autoconfigure.MongoClientSettingsBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;

@Configuration
public class MongoConfig {

    @Bean
    MongoTransactionManager transactionManager(
            MongoDatabaseFactory mongoDatabaseFactory) {
        return new MongoTransactionManager(mongoDatabaseFactory);
    }

    @Bean
    MongoClientSettingsBuilderCustomizer observabilitySettings(
            ObservationRegistry registry) {

        return clientSettingsBuilder ->
                clientSettingsBuilder.observabilitySettings(
                        ObservabilitySettings.micrometerBuilder()
                                .observationRegistry(registry)
                                .build()
                );
    }
}