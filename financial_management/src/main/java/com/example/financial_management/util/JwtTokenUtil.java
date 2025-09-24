package com.example.financial_management.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.financial_management.model.auth.Auth;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenUtil {

    private final Key secretKey;
    private final long expirationMs;

    public JwtTokenUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expirationMs) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.expirationMs = expirationMs;
    }

    // Tạo JWT
    public String generateToken(Auth auth) {
        return Jwts.builder()
                .setSubject(auth.getId())
                .claim("name", auth.getName())
                .claim("email", auth.getEmail())
                .claim("status", auth.getStatus())
                .claim("role", auth.getRole())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Auth extractAuth(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(secretKey).build()
                .parseClaimsJws(token).getBody();

        Auth auth = new Auth();
        auth.setId(claims.getSubject());
        auth.setName(claims.get("name", String.class));
        auth.setEmail(claims.get("email", String.class));
        auth.setStatus(claims.get("status", Integer.class));
        auth.setRole(claims.get("role",Integer.class));
        return auth;
    }

    // Lấy userId từ token
    public UUID extractUserId(String token) {
        return UUID.fromString(extractAllClaims(token).getSubject());
    }

    // Lấy email
    public String extractEmail(String token) {
        return extractAllClaims(token).get("email", String.class);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
