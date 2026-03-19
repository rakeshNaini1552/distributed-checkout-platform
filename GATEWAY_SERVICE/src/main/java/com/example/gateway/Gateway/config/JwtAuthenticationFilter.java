package com.example.gateway.Gateway.config;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtAuthenticationFilter implements GlobalFilter {

    // ✅ Use the SAME secret key as your User Service
    private final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(
            "this_is_a_super_long_secret_key_with_32+_chars!".getBytes()
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        System.out.println(">> Gateway Filter triggered for path: " + path);

        // ✅ 1. Allow public login endpoint without token
        if (path.startsWith("/auth/login")) {
            return chain.filter(exchange);
        }

        // ✅ 2. Extract Authorization header
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return this.unauthorized(exchange, "Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);

        try {
            // ✅ 3. Parse and validate JWT
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // ✅ 4. (Optional) Forward user claims as headers to downstream services
            exchange = exchange.mutate()
                    .request(request.mutate()
                            .header("X-User-Id", claims.getSubject())
                            .build())
                    .build();

        } catch (JwtException e) {
            return this.unauthorized(exchange, "Invalid or Expired JWT");
        }

        // ✅ 5. Token is valid → forward request
        return chain.filter(exchange);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
    }
}

