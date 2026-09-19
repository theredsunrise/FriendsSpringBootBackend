package org.example.gateway.infrastructure.in.routing.config;

import org.example.gateway.infrastructure.in.routing.dto.RouteConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "gateway")
public record GatewayRouteProperties(
        Map<String, RouteConfig> routes
) {
}
