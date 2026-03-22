package com.example.order.service;

import com.example.order.client.AiServiceClient;
import com.example.order.client.ProductClient;
import com.example.order.dto.AiRecommendRequest;
import com.example.order.dto.AiRecommendResponse;
import com.example.order.dto.ProductDto;
import com.example.order.model.Order;
import com.example.order.model.OrderRecommendation;
import com.example.order.repository.OrderRecommendationRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AiService {

    private final AiServiceClient aiServiceClient;
    private final ProductClient productClient;
    private final OrderRecommendationRepository recommendationRepository;

    public AiService(AiServiceClient aiServiceClient,
                     ProductClient productClient,
                     OrderRecommendationRepository recommendationRepository) {
        this.aiServiceClient = aiServiceClient;
        this.productClient = productClient;
        this.recommendationRepository = recommendationRepository;
    }

    @Async
    public void generateRecommendations(Order order) {
        try {
            // 1. Get all products from Product Service
            List<String> availableProducts = productClient.getAllProducts()
                    .stream()
                    .map(ProductDto::getName)
                    .toList();

            // 2. Build request
            AiRecommendRequest request = AiRecommendRequest.builder()
                    .orderId(order.getId())
                    .userId(order.getUserId())
                    .purchasedItem(order.getItem())
                    .category("Electronics")
                    .availableProducts(availableProducts)
                    .build();

            // 3. Call AI Service
            AiRecommendResponse response = aiServiceClient.getRecommendations(request);

            // 4. Save each recommendation
            List<OrderRecommendation> recommendations = response.getRecommendations()
                    .stream()
                    .map(productName -> OrderRecommendation.builder()
                            .orderId(order.getId())
                            .productName(productName)
                            .createdAt(LocalDateTime.now())
                            .build())
                    .toList();

            recommendationRepository.saveAll(recommendations);

            System.out.println("Saved " + recommendations.size()
                    + " recommendations for Order " + order.getId());

        } catch (Exception e) {
            System.out.println("AI recommendation failed for order "
                    + order.getId() + ": " + e.getMessage());
        }
    }
}