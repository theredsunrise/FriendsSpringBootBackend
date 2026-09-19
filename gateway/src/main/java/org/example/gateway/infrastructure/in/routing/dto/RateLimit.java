package org.example.gateway.infrastructure.in.routing.dto;

public record RateLimit(
        int replenishRate,
        int burstCapacity,
        int requestedTokens
) {
}