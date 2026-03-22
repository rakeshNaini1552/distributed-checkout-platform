package com.example.order.kafka;

import com.example.events.OrderConfirmedEvent;
import com.example.events.OrderRejectedEvent;
import com.example.events.OrderStatus;
import com.example.order.repository.OrderRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderStatusConsumer {

    private final OrderRepository orderRepository;

    public OrderStatusConsumer(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @KafkaListener(topics = "order-confirmed-events", groupId = "order-service-group")
    public void handleOrderConfirmed(OrderConfirmedEvent event) {
        System.out.println("Order CONFIRMED: " + event.getOrderId());
        orderRepository.findById(Integer.parseInt(event.getOrderId()))
                .ifPresent(order -> {
                    order.setStatus(OrderStatus.CONFIRMED);
                    orderRepository.save(order);
                });
    }

    @KafkaListener(topics = "order-rejected-events", groupId = "order-service-group")
    public void handleOrderRejected(OrderRejectedEvent event) {
        System.out.println("Order REJECTED: " + event.getOrderId()
                + " | Reason: " + event.getReason());
        orderRepository.findById(Integer.parseInt(event.getOrderId()))
                .ifPresent(order -> {
                    order.setStatus(OrderStatus.REJECTED);
                    orderRepository.save(order);
                });
    }
}