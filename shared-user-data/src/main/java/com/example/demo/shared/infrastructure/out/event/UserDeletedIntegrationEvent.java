package com.example.demo.shared.infrastructure.out.event;


public record UserDeletedIntegrationEvent(Long userId) implements IntegrationEvent {
}
