package com.think.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret:defaultSecretKeyForHealthcareProviderSystem}")
    private String secret;

    @Value("${jwt.expiration:3600}")
    private long expiration; // 1 hour in seconds

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(UUID providerId, String email, String specialization) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("provider_id", providerId.toString());
        claims.put("email", email);
        claims.put("role", "PROVIDER");
        claims.put("specialization", specialization);

        return createToken(claims, email);
    }

    public String generateToken(Map<String, Object> claims, String subject) {
        return createToken(claims, subject);
    }

    public String generateToken(Map<String, Object> claims, String subject, long customExpiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + (customExpiration * 1000));

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + (expiration * 1000));

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public UUID extractProviderId(String token) {
        String providerIdStr = extractClaim(token, claims -> claims.get("provider_id", String.class));
        return UUID.fromString(providerIdStr);
    }

    public String extractPatientId(String token) {
        return extractClaim(token, claims -> claims.get("patient_id", String.class));
    }

    public String extractSpecialization(String token) {
        return extractClaim(token, claims -> claims.get("specialization", String.class));
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, java.util.function.Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenExpired(String token) {
        try {
            Date expiration = extractExpiration(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public long getExpirationTime() {
        return expiration;
    }
}
