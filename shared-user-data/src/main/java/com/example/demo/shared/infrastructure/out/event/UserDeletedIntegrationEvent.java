package com.example.demo.shared.infrastructure.out.event;

import java.util.UUID;

public record UserDeletedIntegrationEvent(UUID userId) implements IntegrationEvent {
}

