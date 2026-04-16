package com.jamify.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration-ms}")
    private long jwtExpirationMs;

    private final RedisTemplate<String , String> redisTemplate;

    public JwtService (RedisTemplate<String , String> redisTemplate){
        this.redisTemplate = redisTemplate;
    }


    // ─── Generate signing key from secret ──────────────────────────────

    private SecretKey getSigningKey(){
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }


    // ─── Generate JWT for a user ────────────────────────────────────────

    public String generateToken(UUID userId , String spotifyId){
        String sessionId = UUID.randomUUID().toString();
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(userId.toString())
                .claim("spotifyId" , spotifyId)
                .claim("sessionId" , sessionId)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }


    // ─── Validate a JWT ─────────────────────────────────────────────────

    public boolean validateToken(String token){
        try{
            Claims claims = extractAllClaims(token);
            String sessionId = claims.get("sessionId" , String.class);

            // Check if session is blacklisted
            if(isSessionBlacklisted(sessionId)){
                log.warn("Rejected blacklisted session: {}" , sessionId);
                return false;
            }
            return  true;
        }catch(Exception e){
            log.warn("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }


    // ─── Extract all claims from token ──────────────────────────────────

    public Claims extractAllClaims(String token){
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }


    // ─── Extract userId from token ───────────────────────────────────────

    public UUID extractUserId(String token){
        String subject = extractAllClaims(token).getSubject();
        return UUID.fromString(subject);
    }


    // ─── Extract sessionId from token ───────────────────────────────────

    public String extractSessionId(String token){
        return extractAllClaims(token).get("sessionId" , String.class);
    }


    // ─── Blacklist a session ─────────────────────────────────────────────

    public void blacklistSession(String token){
        try {
            Claims claims = extractAllClaims(token);
            String sessionId = claims.get("sessionId" , String.class);
            Date expiry = claims.getExpiration();

            long ttlMs = expiry.getTime() - System.currentTimeMillis();
            if(ttlMs > 0){
                String uniqueKey = "blacklist:" + sessionId;
                redisTemplate.opsForValue().set(
                        uniqueKey,
                        "revoked",
                        ttlMs,
                        TimeUnit.MILLISECONDS
                );
                log.info("Blacklisted session: {}", sessionId);
            }
        }catch (Exception e) {
            log.error("Failed to blacklist session: {}", e.getMessage());
        }
    }


    // ─── Check if session is blacklisted ────────────────────────────────

    private boolean isSessionBlacklisted(String sessionId) {
        String uniqueKey = "blacklist:" + sessionId;
        return Boolean.TRUE.equals(
                redisTemplate.hasKey(uniqueKey)
        );
    }
}

