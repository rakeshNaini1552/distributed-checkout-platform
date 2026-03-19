package com.example.userService.kafka;

import com.example.events.OrderCreatedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class OrderEventListener {

    @KafkaListener(topics = "order-events", groupId = "user-service-group")
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {
        System.out.println("Received OrderCreatedEvent in UserService:");
        System.out.println("  Order ID: " + event.getOrderId());
        System.out.println("  User ID: " + event.getUserId());
        System.out.println("  Item: " + event.getItem());
        System.out.println("  Price: " + event.getPrice());
        System.out.println("  Created At: " + event.getCreatedAt());

        // - Here you could:
        // - send user notifications
        // - trigger email
        // - enrich user history
    }
}
