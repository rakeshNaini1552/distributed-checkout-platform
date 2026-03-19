package com.example.order.client;

import com.example.order.config.FeignConfig;
import com.example.order.dto.UserDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "USER-SERVICE", configuration = FeignConfig.class)  // Eureka resolves this dynamically
public interface UserClient {

    @GetMapping("/users/{id}")
    @Retry(name = "userServiceRetry", fallbackMethod = "fallbackUser")
    @CircuitBreaker(name = "userServiceCB", fallbackMethod = "fallbackUser")
    UserDto getUserById(@PathVariable("id") Integer id);

    default UserDto fallbackUser(Integer id, Throwable ex) {
        System.out.println("⚠️ UserService is DOWN, returning fallback for user: " + id);
        return UserDto.builder()
                .id(id)
                .email("unknown@email.com")
                .firstName("Unknown")
                .lastName("User")
                .build();
    }
}