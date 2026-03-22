package com.example.order.client;

import com.example.order.dto.ProductDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.util.List;

@FeignClient(name = "PRODUCT-SERVICE")
public interface ProductClient {

    @GetMapping("/products/{id}")
    @Retry(name = "userServiceRetry")
    @CircuitBreaker(name = "userServiceCB", fallbackMethod = "fallbackProduct")
    ProductDto getProductById(@PathVariable("id") Integer id);

    @GetMapping("/products")
    @Retry(name = "userServiceRetry")
    @CircuitBreaker(name = "userServiceCB", fallbackMethod = "fallbackProducts")
    List<ProductDto> getAllProducts();

    default List<ProductDto> fallbackProducts(Throwable ex) {
        System.out.println("Product Service is DOWN, returning empty catalog");
        return List.of();
    }

    default ProductDto fallbackProduct(Integer id, Throwable ex) {
        System.out.println("Product Service is DOWN, returning fallback for product: " + id);
        return ProductDto.builder()
                .id(id)
                .name("Unknown Product")
                .price(BigDecimal.ZERO)
                .category("Unknown")
                .build();
    }
}