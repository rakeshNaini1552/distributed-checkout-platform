package com.example.inventory_service.kafka;

import com.example.events.OrderConfirmedEvent;
import com.example.events.OrderRejectedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class InventoryEventPublisher {

    private static final String CONFIRMED_TOPIC = "order-confirmed-events";
    private static final String REJECTED_TOPIC = "order-rejected-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public InventoryEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishConfirmed(OrderConfirmedEvent event) {
        kafkaTemplate.send(CONFIRMED_TOPIC, event.getOrderId(), event);
        System.out.println("OrderConfirmedEvent published for Order ID: " + event.getOrderId());
    }

    public void publishRejected(OrderRejectedEvent event) {
        kafkaTemplate.send(REJECTED_TOPIC, event.getOrderId(), event);
        System.out.println("OrderRejectedEvent published for Order ID: " + event.getOrderId()
                + " | Reason: " + event.getReason());
    }
}