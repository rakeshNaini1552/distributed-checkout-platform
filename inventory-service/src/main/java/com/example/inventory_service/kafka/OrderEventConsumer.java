package com.example.inventory_service.kafka;

import com.example.events.OrderCreatedEvent;
import com.example.inventory_service.service.InventoryService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventConsumer {

    private final InventoryService inventoryService;

    public OrderEventConsumer(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @KafkaListener(topics = "order-events", groupId = "inventory-service-group")
    public void handleOrderCreated(OrderCreatedEvent event) {
        System.out.println("Inventory Service received order: " + event.getOrderId()
                + " | Item: " + event.getItem());
        inventoryService.processOrder(event);
    }
}