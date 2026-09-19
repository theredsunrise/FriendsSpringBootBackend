package com.example.demo.shared.infrastructure.out.repository.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import tools.jackson.databind.ObjectMapper;

@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
public class RedisCacheConfig {

    private final RedisCacheProperties cacheProperties;

    @Bean
    RedisCacheConfiguration redisCacheConfiguration(
            ObjectMapper objectMapper
    ) {
        RedisCacheConfiguration configuration =
                RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(cacheProperties.timeToLive())
                        .serializeValuesWith(
                                RedisSerializationContext.SerializationPair.fromSerializer(
                                        new GenericJacksonJsonRedisSerializer(objectMapper)
                                )
                        );

        if (cacheProperties.useKeyPrefix()) {
            configuration = configuration.prefixCacheNameWith(
                    cacheProperties.keyPrefix()
            );
        }

        if (!cacheProperties.cacheNullValues()) {
            configuration = configuration.disableCachingNullValues();
        }

        return configuration;
    }
}
