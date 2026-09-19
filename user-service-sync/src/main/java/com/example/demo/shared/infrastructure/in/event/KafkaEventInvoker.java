package com.example.demo.shared.infrastructure.in.event;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.core.KafkaTemplate;

@Slf4j
public final class KafkaEventInvoker {

    private KafkaEventInvoker() {
    }

    public static <V> void send(KafkaTemplate<String, V> kafkaTemplate, String topic, String key, V message) {
        kafkaTemplate.send(topic, key, message)
                .thenAccept(result -> {
                    RecordMetadata metadata = result.getRecordMetadata();

                    log.info(
                            "Kafka message sent: topic={}, partition={}, offset={}",
                            metadata.topic(),
                            metadata.partition(),
                            metadata.offset()
                    );
                })
                .exceptionally(exception -> {
                    log.error(
                            "Kafka message sending failed: topic={}, key={}",
                            topic,
                            key,
                            exception
                    );
                    return null;
                });
    }
}
