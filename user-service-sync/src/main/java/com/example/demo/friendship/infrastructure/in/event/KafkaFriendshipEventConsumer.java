package com.example.demo.friendship.infrastructure.in.event;

import com.example.demo.friendship.application.exception.FriendshipAlreadyExistsException;
import com.example.demo.friendship.application.port.in.FriendshipUseCase;
import com.example.demo.friendship.domain.Friendship;
import com.example.demo.friendship.infrastructure.in.event.dto.FriendshipEventResponse;
import com.example.demo.shared.domain.OutBoxEventType;
import com.example.demo.shared.infrastructure.in.event.KafkaEventInvoker;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;


import static com.example.demo.shared.infrastructure.in.event.KafkaTemplateConfig.JSON_TEMPLATE;
import static com.example.demo.shared.infrastructure.in.event.KafkaTemplateConfig.STRING_TEMPLATE;

@Component
@Slf4j
public class KafkaFriendshipEventConsumer {

    private final FriendshipUseCase friendshipUseCase;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, Object> jsonEventProducer;

    public KafkaFriendshipEventConsumer(
            FriendshipUseCase friendshipUseCase,
            ObjectMapper objectMapper,
            @Qualifier(JSON_TEMPLATE)
            KafkaTemplate<String, Object> eventProducer) {

        this.friendshipUseCase = friendshipUseCase;
        this.objectMapper = objectMapper;
        this.jsonEventProducer = eventProducer;
    }

    @Value("${app.events.response.friendship-events-topic}")
    private String responseTopic;

    @RetryableTopic(
            attempts = "2",
            backOff = @BackOff(delay = 500),
            kafkaTemplate = STRING_TEMPLATE
    )
    @KafkaListener(
            id = "friendship-events-consumer",
            topics = "${app.events.friendship-events-topic}"
    )
    public void listen(@Payload String message,
                       @Header("id") String outboxIdStr,
                       @Header(KafkaHeaders.RECEIVED_KEY) String kafkaKey,
                       @Header("eventType") String eventType) {
        Friendship friendship;
        OutBoxEventType event;
        Long outboxId = null;
        Long userId = null;
        Long friendId = null;

        try {
            outboxId = Long.valueOf(outboxIdStr);
            log.info("**** Received outbox id {}", outboxId);
            event = OutBoxEventType.valueOf(eventType);
            log.info("**** Received event {}", outboxId);
            friendship = objectMapper.readValue(message, Friendship.class);
            log.info("**** Received friendship id {}", friendship.getId());
            userId = friendship.getUserId();
            friendId = friendship.getFriendId();
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "**** Failed parsing arguments for outboxId: %s, userId: %s, friendId: %s".formatted(outboxId, userId, friendId),
                    e);
        }

        try {
            log.info("**** Received friendship event for id: {}, userId: {}, friendId: {}", outboxId, userId, friendId);
            switch (event) {
                case CREATED:
                    friendshipUseCase.add(friendship);
                    break;
                case DELETED:
                    friendshipUseCase.deleteFromUser(userId, friendId);
                    break;
            }
        } catch (FriendshipAlreadyExistsException _) {
            log.warn("Friend with ID: {} is already a friend with: {}.", friendId, userId);

        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Unknown error occurred for outboxId: %s, userId: %s, friendId: %s.".formatted(outboxId, userId, friendId),
                    e);
        }
        KafkaEventInvoker.send(
                jsonEventProducer,
                responseTopic,
                kafkaKey,
                new FriendshipEventResponse(
                        outboxId,
                        userId,
                        friendId,
                        Boolean.TRUE
                ));
    }

    @DltHandler
    public void handleDlt(
            ConsumerRecord<String, Object> record,
            @Header("id") String outboxIdStr,
            @Header("eventType") String eventType,
            @Header(
                    name = KafkaHeaders.EXCEPTION_MESSAGE,
                    required = false
            )
            String errorMessage) {

        Long outboxId = null;
        Long userId = null;
        Long friendId = null;

        log.error("**** Dead letter friendship record occurred: Outbox ID:{}, event type:{}, error: {}", outboxIdStr, eventType, errorMessage);
        try {
            outboxId = Long.valueOf(outboxIdStr);
            Friendship friendship = objectMapper.readValue(record.value().toString(), Friendship.class);
            userId = friendship.getUserId();
            friendId = friendship.getFriendId();
        } catch (Exception e) {
            log.warn("**** Could not deserialize DLT payload as Friendship class.", e);
        }

        String kafkaKey = record.key();
        KafkaEventInvoker.send(
                jsonEventProducer,
                responseTopic,
                kafkaKey,
                new FriendshipEventResponse(
                        outboxId,
                        userId,
                        friendId,
                        Boolean.FALSE
                ));

        record.headers().forEach(header ->
                log.error(
                        "**** DLT record header: {}={}",
                        header.key(),
                        header.value()
                )
        );
    }
}
