package com.example.order.client;

import com.example.order.dto.AiRecommendRequest;
import com.example.order.dto.AiRecommendResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class AiServiceClient {

    private final WebClient webClient;

    public AiServiceClient(@Value("${ai.service.url}") String aiServiceUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(aiServiceUrl)
                .build();
    }

    public AiRecommendResponse getRecommendations(AiRecommendRequest request) {
        return webClient.post()
                .uri("/ai/recommend")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(AiRecommendResponse.class)
                .block();
    }
}