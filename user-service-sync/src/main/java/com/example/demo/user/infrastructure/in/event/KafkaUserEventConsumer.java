package com.example.demo.user.infrastructure.in.event;

import com.example.demo.shared.domain.OutBoxEventType;
import com.example.demo.shared.infrastructure.in.event.KafkaEventInvoker;
import com.example.demo.user.application.exception.UserAlreadyExistsException;
import com.example.demo.user.application.port.in.UserUseCase;
import com.example.demo.user.domain.User;
import com.example.demo.user.infrastructure.in.event.dto.UserEventResponse;
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
public class KafkaUserEventConsumer {

    private final UserUseCase userUseCase;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, Object> jsonEventProducer;

    public KafkaUserEventConsumer(
            UserUseCase userUseCase,
            ObjectMapper objectMapper,
            @Qualifier(JSON_TEMPLATE)
            KafkaTemplate<String, Object> eventProducer) {

        this.userUseCase = userUseCase;
        this.objectMapper = objectMapper;
        this.jsonEventProducer = eventProducer;
    }

    @Value("${app.events.response.user-events-topic}")
    private String responseTopic;

    @RetryableTopic(
            attempts = "2",
            backOff = @BackOff(delay = 500),
            kafkaTemplate = STRING_TEMPLATE
    )
    @KafkaListener(
            id = "user-events-consumer",
            topics = "${app.events.user-events-topic}"
    )
    public void listen(@Payload String message,
                       @Header("id") String outboxIdStr,
                       @Header(KafkaHeaders.RECEIVED_KEY) String kafkaKey,
                       @Header("eventType") String eventType) {
        User user;
        OutBoxEventType event;
        Long outboxId = null;
        Long userId = null;

        try {
            outboxId = Long.valueOf(outboxIdStr);
            log.info("**** Received outbox id {}", outboxId);
            event = OutBoxEventType.valueOf(eventType);
            log.info("**** Received event {}", outboxId);
            user = objectMapper.readValue(message, User.class);
            log.info("**** Received user id {}", user.getId());
            userId = user.getId();
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Failed parsing arguments for outboxId: %s, userId: %s.".formatted(outboxId, userId),
                    e);
        }

        try {
            log.info("**** Received user event for id: {}, userId: {}.", outboxId, userId);
            switch (event) {
                case CREATED:
                    userUseCase.create(user);
                    break;
                case DELETED:
                    userUseCase.delete(userId);
                    break;
            }
        } catch (UserAlreadyExistsException _) {
            log.warn("User {} already exists", user.getId());

        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Unknown error occurred for outboxId: %s, userId: %s.".formatted(outboxId, userId),
                    e);
        }
        KafkaEventInvoker.send(
                jsonEventProducer,
                responseTopic,
                kafkaKey,
                new UserEventResponse(
                        outboxId,
                        userId,
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

        log.error("**** Dead letter user record occurred: Outbox ID:{}, event type:{}, error: {}", outboxIdStr, eventType, errorMessage);
        try {
            outboxId = Long.valueOf(outboxIdStr);
            User user = objectMapper.readValue(record.value().toString(), User.class);
            userId = user.getId();
        } catch (Exception e) {
            log.warn("**** Could not deserialize DLT payload as User class.", e);
        }

        String kafkaKey = record.key();
        KafkaEventInvoker.send(
                jsonEventProducer,
                responseTopic,
                kafkaKey,
                new UserEventResponse(
                        outboxId,
                        userId,
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
