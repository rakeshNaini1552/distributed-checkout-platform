package com.example.userService.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.Date;
import io.jsonwebtoken.*;


@Component
public class JwtUtil {

    //Use a secure key (at least 256 bits)
    private final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(
            "this_is_a_super_long_secret_key_with_32+_chars!".getBytes()
    );

    private final long EXPIRATION_TIME = 1000 * 60 * 15; // 1 hour in ms


    public String generateToken(Integer userId, String username) {
        return Jwts.builder()
                .setSubject(username)             // still keep username as subject
                .claim("userId", userId)          // ✅ custom claim for userId
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }


    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public boolean validateToken(String token, org.springframework.security.core.userdetails.UserDetails userDetails) {
        String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
