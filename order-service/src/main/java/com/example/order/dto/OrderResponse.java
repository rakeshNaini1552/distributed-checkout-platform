package com.example.order.dto;

import com.example.order.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderResponse {
    private Integer id;
    private Integer userId;
    private String item;
    private BigDecimal price;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private UserDto userDto;


}
