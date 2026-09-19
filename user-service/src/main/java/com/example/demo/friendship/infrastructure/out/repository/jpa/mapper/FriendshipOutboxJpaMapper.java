package com.example.demo.friendship.infrastructure.out.repository.jpa.mapper;

import com.example.demo.friendship.domain.Friendship;
import com.example.demo.friendship.infrastructure.out.repository.jpa.dto.FriendshipOutboxJpaEntity;
import com.example.demo.shared.domain.OutBoxEventType;
import com.example.demo.shared.domain.OutBoxStatus;
import com.example.demo.shared.infrastructure.out.openTelemetry.OpenTelemetryHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public final class FriendshipOutboxJpaMapper {

    private final ObjectMapper objectMapper;
    private final OpenTelemetryHelper helper;

    public FriendshipOutboxJpaEntity toJpaEntity(
            String eventGroup,
            String eventsTopic,
            OutBoxEventType eventType,
            OutBoxStatus eventStatus,
            Friendship friendship) throws IOException {
        String jsonFriendship = objectMapper.writeValueAsString(friendship);

        return FriendshipOutboxJpaEntity.builder()
                .eventTopic(eventsTopic)
                .eventId(friendship.getId().toString())
                .eventPayload(jsonFriendship)
                .tracingSpanContext(helper.getTraceParent())
                .eventType(eventType)
                .eventGroup(eventGroup)
                .eventStatus(eventStatus)
                .build();
    }
}