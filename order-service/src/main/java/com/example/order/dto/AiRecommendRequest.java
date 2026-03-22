package com.example.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AiRecommendRequest {
    private Integer orderId;
    private Integer userId;
    private String purchasedItem;
    private String category;
    private List<String> availableProducts;
}