package org.example.gateway.infrastructure.in.routing;

import lombok.extern.slf4j.Slf4j;
import org.example.gateway.infrastructure.in.routing.config.GatewayRouteProperties;
import org.example.gateway.infrastructure.in.routing.dto.RouteConfig;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Slf4j
@Component
public class DefaultRouting {
    public static final String X_GATEWAY_BASE_URL = "X-Gateway-Base-Url";

    public void routes(
            RouteLocatorBuilder.Builder routes,
            KeyResolver ipKeyResolver,
            GatewayRouteProperties routeProperties,
            Map<String, RedisRateLimiter> rateLimiters) {

        Map<String, RouteConfig> routeConfigs = routeProperties.routes();
        routeConfigs.forEach((group, config) -> {

            routes.route(config.id(), r -> r
                    .host(config.hosts())
                    .and()
                    .path("/%s/**".formatted(group))
                    .filters(f -> f
                            .stripPrefix(1)
                            .circuitBreaker(cb -> cb
                                    .setName(group)
                            )
                            .requestRateLimiter(rl -> rl
                                    .setRateLimiter(
                                            rateLimiters.get(group)
                                    )
                                    .setKeyResolver(ipKeyResolver)
                            )
                            .filter((exchange, chain) -> {
                                String baseUrlWithGroup = UriComponentsBuilder
                                        .fromUri(exchange.getRequest().getURI())
                                        .replacePath("/" + group)
                                        .replaceQuery(null)
                                        .build()
                                        .toUriString();

                                ServerHttpRequest mutatedRequest =
                                        exchange.getRequest().mutate()
                                                .header(X_GATEWAY_BASE_URL, baseUrlWithGroup)
                                                .build();
                                return chain.filter(exchange.mutate().request(mutatedRequest).build());
                            })
                    )
                    .uri(config.destination())
            );
        });

    }
}
