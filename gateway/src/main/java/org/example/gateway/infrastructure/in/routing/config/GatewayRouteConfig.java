package org.example.gateway.infrastructure.in.routing.config;

import lombok.RequiredArgsConstructor;
import org.example.gateway.infrastructure.in.routing.DefaultRouting;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
public class GatewayRouteConfig {

    public static final String ROUTE_LOCATOR = "customRouteLocator";

    private final DefaultRouting defaultRouting;

    @Bean(ROUTE_LOCATOR)
    public RouteLocator customRouteLocator(
            RouteLocatorBuilder builder,
            KeyResolver ipKeyResolver,
            GatewayRouteProperties routeProperties,
            Map<String, RedisRateLimiter> rateLimiters
    ) {
        RouteLocatorBuilder.Builder routes = builder.routes();
        defaultRouting.routes(routes,
                ipKeyResolver,
                routeProperties,
                rateLimiters);
        return routes.build();
    }
}
