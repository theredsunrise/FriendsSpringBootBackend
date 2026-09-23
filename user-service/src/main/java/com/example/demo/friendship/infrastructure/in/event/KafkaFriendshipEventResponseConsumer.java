package com.example.demo.friendship.infrastructure.in.event;

import com.example.demo.friendship.application.port.out.repository.FriendshipOutboxRepository;
import com.example.demo.friendship.infrastructure.in.event.dto.FriendshipEventResponse;
import com.example.demo.shared.domain.OutBoxStatus;
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


import static com.example.demo.friendship.infrastructure.in.event.KafkaFriendshipEventKafkaConfig.FRIENDSHIP_KAFKA_LISTENER_FACTORY;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaFriendshipEventResponseConsumer {

    private final ObjectMapper objectMapper;
    private final FriendshipOutboxRepository outboxRepository;

    @RetryableTopic(
            attempts = "2",
            backOff = @BackOff(delay = 500),
            exclude = DeserializationException.class
    )
    @Transactional
    @KafkaListener(
            id = "friendship-events-response-consumer",
            topics = "${app.events.response.friendship-events-topic}",
            containerFactory = FRIENDSHIP_KAFKA_LISTENER_FACTORY
    )
    public void listen(FriendshipEventResponse message) {
        Long id = message.id();
        Long userId = message.userId();
        Long friendId = message.friendId();
        log.info("**** Received response from friendship event for id: {}, userId: {}, friendId: {}.",
                id, userId, friendId);
        if (message.result()) {
            outboxRepository.updateStatus(id, OutBoxStatus.COMPLETED);
        } else {
            outboxRepository.updateStatus(id, OutBoxStatus.FAILED);
        }
    }

    @DltHandler
    public void handleDlt(ConsumerRecord<String, Object> record) {
        FriendshipEventResponse eventResponse = deserializeEvent(record);

        if (eventResponse != null) {
            log.error(
                    "**** Friendship event moved to DLT: userId={}, friendId={}, result={}",
                    eventResponse.userId(),
                    eventResponse.friendId(),
                    eventResponse.result()
            );
        } else {
            log.error("**** Friendship event moved to DLT: payload could not be deserialized");
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

    private FriendshipEventResponse deserializeEvent(
            ConsumerRecord<String, Object> record
    ) {
        try {
            return switch (record.value()) {
                case FriendshipEventResponse response -> response;
                case byte[] bytes -> objectMapper.readValue(bytes, FriendshipEventResponse.class);
                case null, default -> null;
            };
        } catch (Exception e) {
            log.warn("Could not deserialize DLT payload as FriendshipEventResponse class.", e);
            return null;
        }
    }
}
