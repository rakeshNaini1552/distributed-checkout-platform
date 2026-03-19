package com.example.order.events;

import com.example.order.model.OrderStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderCreatedEvent {
    private String orderId;         // from Order entity
    private Integer userId;         // which user placed the order
    private String item;            // product name
    private BigDecimal price;       // order price

    @Enumerated(EnumType.STRING)
    private OrderStatus status;    private LocalDateTime createdAt; // when the order was placed
}

