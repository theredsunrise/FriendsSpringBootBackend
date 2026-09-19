package com.example.demo.shared.infrastructure.in.event;

import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

import java.util.Map;

@Configuration
public class KafkaTemplateConfig {

    public static final String JSON_TEMPLATE = "jsonTemplate";
    public static final String STRING_TEMPLATE = "stringTemplate";

    @Bean(STRING_TEMPLATE)
    public KafkaTemplate<String, String> stringKafkaTemplate(
            KafkaProperties properties) {

        Map<String, Object> props = properties.buildProducerProperties();
        ProducerFactory<String, String> producerFactory =
                new DefaultKafkaProducerFactory<>(
                        props,
                        new StringSerializer(),
                        new StringSerializer()
                );
        var template = new KafkaTemplate<>(producerFactory);
        template.setObservationEnabled(true);
        return template;
    }

    @Bean(JSON_TEMPLATE)
    public KafkaTemplate<String, Object> jsonKafkaTemplate(
            KafkaProperties properties) {

        Map<String, Object> props = properties.buildProducerProperties();
        ProducerFactory<String, Object> producerFactory =
                new DefaultKafkaProducerFactory<>(
                        props,
                        new StringSerializer(),
                        new JacksonJsonSerializer<>()
                );

        var template = new KafkaTemplate<>(producerFactory);
        template.setObservationEnabled(true);
        return template;
    }
}
