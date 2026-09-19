package com.example.demo.user.infrastructure.in.event;

import com.example.demo.user.infrastructure.in.event.dto.UserEventResponse;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.kafka.autoconfigure.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;

import java.util.Map;

@Configuration
public class KafkaUserEventKafkaConfig {
    private static final String TRUSTED_PACKAGES = "com.example.demo.user.infrastructure.in.event.dto";
    public static final String USER_EVENT_CONSUMER_FACTORY = "userEventConsumerFactory";
    public static final String USER_EVENT_LISTENER_FACTORY = "userEventKafkaListenerContainerFactory";

    @Bean(USER_EVENT_CONSUMER_FACTORY)
    public ConsumerFactory<String, UserEventResponse> userEventConsumerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> props = kafkaProperties.buildConsumerProperties();

        JacksonJsonDeserializer<UserEventResponse> jsonDeserializer =
                new JacksonJsonDeserializer<>(UserEventResponse.class);

        jsonDeserializer.addTrustedPackages(TRUSTED_PACKAGES);
        ErrorHandlingDeserializer<UserEventResponse> errorHandlingDeserializer =
                new ErrorHandlingDeserializer<>(jsonDeserializer);

        var factory = new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                errorHandlingDeserializer
        );
        return factory;
    }

    @Bean(USER_EVENT_LISTENER_FACTORY)
    public ConcurrentKafkaListenerContainerFactory<String, UserEventResponse> userEventKafkaListenerContainerFactory(
            ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
            @Qualifier(USER_EVENT_CONSUMER_FACTORY) ConsumerFactory<String, UserEventResponse> userEventConsumerFactory) {

        var factory = new ConcurrentKafkaListenerContainerFactory<String, UserEventResponse>();
        @SuppressWarnings("rawtypes")
        ConcurrentKafkaListenerContainerFactory rawFactory = factory;

        @SuppressWarnings("rawtypes")
        ConsumerFactory rawConsumerFactory = userEventConsumerFactory;
        configurer.configure(rawFactory, rawConsumerFactory);

        return factory;
    }
}
