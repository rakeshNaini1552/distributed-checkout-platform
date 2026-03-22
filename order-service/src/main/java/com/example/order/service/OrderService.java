package com.example.order.service;


import com.example.events.OrderCreatedEvent;
import com.example.events.OrderStatus;
import com.example.order.client.ProductClient;
import com.example.order.client.UserClient;
import com.example.order.config.JwtUser;
import com.example.order.dto.*;
import com.example.order.kafka.OrderEventPublisher;
import com.example.order.model.Order;
import com.example.order.repository.OrderRecommendationRepository;
import com.example.order.repository.OrderRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderEventPublisher orderEventPublisher;
    private final UserClient userClient;
    private final ProductClient productClient;
    private final AiService aiService;
    private final OrderRecommendationRepository recommendationRepository;


    public OrderService(AiService aiService, OrderRepository orderRepository, OrderEventPublisher orderEventPublisher, UserClient userClient, ProductClient productClient, OrderRecommendationRepository recommendationRepository) {
        this.productClient = productClient;
        this.orderRepository = orderRepository;
        this.orderEventPublisher = orderEventPublisher;
        this.userClient = userClient;
        this.aiService = aiService;
        this.recommendationRepository = recommendationRepository;
    }

    @Transactional
    public Order createOrder(OrderRequest orderRequest) {

        JwtUser jwtUser = (JwtUser) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        // 1. Resolve product details from Product Service via Feign
        ProductDto product = productClient.getProductById(orderRequest.getProductId());

        // 2. Save order with real product name and price
        Order order = Order.builder()
                .userId(jwtUser.userId())
                .item(product.getName())
                .price(product.getPrice())
                .createdAt(LocalDateTime.now())
                .status(OrderStatus.CREATED)
                .build();
        orderRepository.save(order);

        // Async AI recommendations — non-blocking
        aiService.generateRecommendations(order);

        // 3. Build and publish Kafka event
        OrderCreatedEvent event = new OrderCreatedEvent(
                order.getId().toString(),
                order.getUserId(),
                order.getItem(),
                order.getPrice(),
                com.example.events.OrderStatus.valueOf(order.getStatus().name()),
                order.getCreatedAt()
        );

        orderEventPublisher.publishOrderCreatedEvent(event);

        return order;
    }

/*   This commented part is for synchronous Rest call where feign comes in picture

 private final UserClient userClient;

    public OrderService(OrderRepository repo, UserClient userClient){
        this.orderRepository = repo;
        this.userClient = userClient;
    }
    public OrderResponse createOrder(OrderRequest orderRequest){

        JwtUser jwtUser = (JwtUser) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        Order order = Order.builder()
                .userId(jwtUser.userId())
                .item(orderRequest.getItem())
                .price(orderRequest.getPrice())
                .createdAt(LocalDateTime.now())
                .status(OrderStatus.CREATED)
                .build();
        return mapToResponse(orderRepository.save(order));
    }

 */

    public OrderResponse getOrderDetailsByOrderId(Integer orderId){
       Order order = orderRepository.findById(orderId).orElseThrow();
        System.out.println("User id is : "+order.getUserId());
        UserDto userDto = userClient.getUserById(order.getUserId());
        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .status(order.getStatus())
                .item(order.getItem())
                .price(order.getPrice())
                .createdAt(order.getCreatedAt())
                .userDto(userDto)
                .build();
    }

    public List<OrderResponse> getAllOrdersbyUserId(Integer userId){
        return orderRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public OrderResponse mapToResponse(Order order){
        UserDto userDto = userClient.getUserById(order.getUserId());
        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .status(order.getStatus())
                .item(order.getItem())
                .price(order.getPrice())
                .userDto(userDto)
                .createdAt(order.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public List<AiRecommendationDto> getRecommendationsByOrderId(Integer orderId) {
        return recommendationRepository.findByOrderId(orderId)
                .stream()
                .map(r -> new AiRecommendationDto(r.getProductName()))
                .toList();
    }

}
