package com.example.order.kafka;

import com.example.order.events.OrderCreatedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderEventPublisher {

    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    public OrderEventPublisher(KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishOrderCreatedEvent(OrderCreatedEvent event) {
        kafkaTemplate.send("order-events", event.getOrderId(), event);
        System.out.println("OrderCreatedEvent published for Order ID: " + event.getOrderId());
    }
}

