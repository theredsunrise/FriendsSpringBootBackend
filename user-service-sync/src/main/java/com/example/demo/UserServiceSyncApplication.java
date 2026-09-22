package com.example.demo;

import com.example.demo.shared.infrastructure.out.repository.redis.RedisCacheProperties;
import io.flamingock.api.annotations.EnableFlamingock;
import io.flamingock.api.annotations.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;

@EnableFlamingock(
        stages = {
                @Stage(
                        location = "com.example.demo.shared.infrastructure.out.repository.migration"
                )
        }
)
@SpringBootApplication
@EnableConfigurationProperties(RedisCacheProperties.class)
public class UserServiceSyncApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceSyncApplication.class, args);
    }
}