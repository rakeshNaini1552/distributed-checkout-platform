package com.example.userService.config;

import com.example.events.OrderCreatedEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@EnableKafka  // ✅ Enables Kafka listener detection
@Configuration
public class KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, OrderCreatedEvent> consumerFactory() {
        Map<String, Object> props = new HashMap<>();

        // ✅ Where is Kafka running?
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        // ✅ Consumer group ID (all instances of this group share load)
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "user-service-group");

        // ✅ Start from beginning if no offset
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // ✅ Key is simple string
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        // ✅ Value is JSON -> OrderCreatedEvent
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        // ✅ Allow all packages for deserialization
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new JsonDeserializer<>(OrderCreatedEvent.class, false)
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderCreatedEvent> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, OrderCreatedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}

