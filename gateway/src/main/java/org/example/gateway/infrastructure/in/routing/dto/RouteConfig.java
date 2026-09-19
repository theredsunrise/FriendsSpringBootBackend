package org.example.gateway.infrastructure.in.routing.dto;

public record RouteConfig(
        String id,
        String[] hosts,
        String destination,
        RateLimit rateLimit
) {
}
