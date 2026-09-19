package org.example.gateway.infrastructure.in.routing.config;

import org.example.gateway.infrastructure.in.routing.dto.RateLimit;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RateLimiterConfig {

    public static final String IP_KEY_RESOLVER = "ipKeyResolver";
    public static final String RATE_LIMITERS = "rateLimiters";

    @Bean(IP_KEY_RESOLVER)
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.justOrEmpty(
                        exchange.getRequest()
                                .getRemoteAddress()
                )
                .map(address -> address.getAddress().getHostAddress());
    }

    @Bean(RATE_LIMITERS)
    public Map<String, RedisRateLimiter> rateLimiters(
            GatewayRouteProperties routeProperties,
            ApplicationContext applicationContext
    ) {
        Map<String, RedisRateLimiter> limiters = new HashMap<>();
        routeProperties.routes().forEach((name, config) -> {
            RateLimit rate = config.rateLimit();
            RedisRateLimiter limiter = new RedisRateLimiter(
                    rate.replenishRate(),
                    rate.burstCapacity(),
                    rate.requestedTokens()
            );
            limiter.setApplicationContext(applicationContext);
            limiters.put(name, limiter);
        });

        return limiters;
    }
}
