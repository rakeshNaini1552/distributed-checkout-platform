package com.example.order.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

@Component
public class JwtUtil {

    //Use a secure key (at least 256 bits)
    private final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(
            "this_is_a_super_long_secret_key_with_32+_chars!".getBytes()
    );

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }
    public Integer extractUserId(String token) {
        return extractAllClaims(token).get("userId", Integer.class);
    }


    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
