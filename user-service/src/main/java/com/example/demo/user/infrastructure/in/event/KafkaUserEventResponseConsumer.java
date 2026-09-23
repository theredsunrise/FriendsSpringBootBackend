package com.example.demo.user.infrastructure.in.event;

import com.example.demo.shared.domain.OutBoxStatus;
import com.example.demo.user.application.port.out.repository.UserOutboxRepository;
import com.example.demo.user.infrastructure.in.event.dto.UserEventResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;


import static com.example.demo.user.infrastructure.in.event.KafkaUserEventKafkaConfig.USER_EVENT_LISTENER_FACTORY;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaUserEventResponseConsumer {

    private final ObjectMapper objectMapper;
    private final UserOutboxRepository outboxRepository;

    @RetryableTopic(
            attempts = "2",
            backOff = @BackOff(delay = 500),
            exclude = DeserializationException.class
    )
    @Transactional
    @KafkaListener(
            id = "user-events-response-consumer",
            topics = "${app.events.response.user-events-topic}",
            containerFactory = USER_EVENT_LISTENER_FACTORY
    )
    public void listen(UserEventResponse message) {
        Long id = message.id();
        Long userId = message.userId();
        log.info("**** Received response from user event for id: {}, userId: {}", id, userId);

        if (message.result()) {
            outboxRepository.updateStatus(id, OutBoxStatus.COMPLETED);
        } else {
            outboxRepository.updateStatus(id, OutBoxStatus.FAILED);
        }
    }

    @DltHandler
    public void handleDlt(ConsumerRecord<String, Object> record) {
        UserEventResponse eventResponse = deserializeEvent(record);

        if (eventResponse != null) {
            log.error(
                    "**** User event moved to DLT: userId={}, result={}",
                    eventResponse.userId(),
                    eventResponse.result()
            );
        } else {
            log.error("**** User event moved to DLT: payload could not be deserialized");
        }

        log.error(
                "**** DLT record metadata: topic={}, partition={}, offset={}, key={}, timestamp={}",
                record.topic(),
                record.partition(),
                record.offset(),
                record.key(),
                record.timestamp()
        );

        record.headers().forEach(header ->
                log.error(
                        "**** DLT record header: {}={}",
                        header.key(),
                        header.value()
                )
        );
    }

    private UserEventResponse deserializeEvent(
            ConsumerRecord<String, Object> record
    ) {
        try {
            return switch (record.value()) {
                case UserEventResponse response -> response;
                case byte[] bytes -> objectMapper.readValue(bytes, UserEventResponse.class);
                case null, default -> null;
            };
        } catch (Exception e) {
            log.warn("Could not deserialize DLT payload as UserEventResponse class.", e);
            return null;
        }
    }
}
