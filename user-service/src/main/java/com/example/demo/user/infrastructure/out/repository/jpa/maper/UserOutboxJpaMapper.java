package com.example.demo.user.infrastructure.out.repository.jpa.maper;

import com.example.demo.shared.domain.OutBoxEventType;
import com.example.demo.shared.domain.OutBoxStatus;
import com.example.demo.shared.infrastructure.out.openTelemetry.OpenTelemetryHelper;
import com.example.demo.user.domain.User;
import com.example.demo.user.infrastructure.out.repository.jpa.dto.UserOutboxJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public final class UserOutboxJpaMapper {

    private final ObjectMapper objectMapper;
    private final OpenTelemetryHelper helper;

    public UserOutboxJpaEntity toJpaEntity(
            String eventGroup,
            String eventsTopic,
            OutBoxEventType eventType,
            OutBoxStatus eventStatus,
            User user) throws IOException {
        String jsonUser = objectMapper.writeValueAsString(user);

        return UserOutboxJpaEntity.builder()
                .eventTopic(eventsTopic)
                .eventId(user.getId().toString())
                .eventPayload(jsonUser)
                .tracingSpanContext(helper.getTraceParent())
                .eventType(eventType)
                .eventGroup(eventGroup)
                .eventStatus(eventStatus)
                .build();
    }
}
