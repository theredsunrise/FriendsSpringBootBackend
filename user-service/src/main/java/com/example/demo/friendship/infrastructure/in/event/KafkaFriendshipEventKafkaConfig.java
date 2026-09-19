package com.example.demo.friendship.infrastructure.in.event;

import com.example.demo.friendship.infrastructure.in.event.dto.FriendshipEventResponse;
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
public class KafkaFriendshipEventKafkaConfig {
    private static final String TRUSTED_PACKAGES = "com.example.demo.friendship.infrastructure.in.event.dto";

    public static final String FRIENDSHIP_KAFKA_CONSUMER_FACTORY = "friendshipEventConsumerFactory";
    public static final String FRIENDSHIP_KAFKA_LISTENER_FACTORY = "friendshipEventKafkaListenerContainerFactory";

    @Bean(FRIENDSHIP_KAFKA_CONSUMER_FACTORY)
    public ConsumerFactory<String, FriendshipEventResponse> friendshipEventConsumerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> props = kafkaProperties.buildConsumerProperties();

        JacksonJsonDeserializer<FriendshipEventResponse> jsonDeserializer =
                new JacksonJsonDeserializer<>(FriendshipEventResponse.class);

        jsonDeserializer.addTrustedPackages(TRUSTED_PACKAGES);
        ErrorHandlingDeserializer<FriendshipEventResponse> errorHandlingDeserializer =
                new ErrorHandlingDeserializer<>(jsonDeserializer);

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                errorHandlingDeserializer
        );
    }

    @Bean(FRIENDSHIP_KAFKA_LISTENER_FACTORY)
    public ConcurrentKafkaListenerContainerFactory<String, FriendshipEventResponse> friendshipEventKafkaListenerContainerFactory(
            ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
            @Qualifier(FRIENDSHIP_KAFKA_CONSUMER_FACTORY) ConsumerFactory<String, FriendshipEventResponse> friendshipEventConsumerFactory) {

        var factory = new ConcurrentKafkaListenerContainerFactory<String, FriendshipEventResponse>();

        @SuppressWarnings("rawtypes")
        ConcurrentKafkaListenerContainerFactory rawFactory = factory;

        @SuppressWarnings("rawtypes")
        ConsumerFactory rawConsumerFactory = friendshipEventConsumerFactory;
        configurer.configure(rawFactory, rawConsumerFactory);

        return factory;
    }
}
