package com.example.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderRejectedEvent {
    private String orderId;
    private Integer userId;
    private String reason;
    private LocalDateTime rejectedAt;
}