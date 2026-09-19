package com.example.demo.shared.infrastructure.out.repository.redis;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "spring.cache.redis")
public record RedisCacheProperties(
        Duration timeToLive,
        String keyPrefix,
        boolean cacheNullValues,
        boolean useKeyPrefix
) {
}

