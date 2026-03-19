package com.example.events;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderCreatedEvent {
    private String orderId;
    private Integer userId;
    private String item;
    private BigDecimal price;
    private OrderStatus status;
    private LocalDateTime createdAt;
}

