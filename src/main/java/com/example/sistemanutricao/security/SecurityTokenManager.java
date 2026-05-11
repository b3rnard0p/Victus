package com.example.sistemanutricao.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class SecurityTokenManager {

    private static final Logger logger = LoggerFactory.getLogger(SecurityTokenManager.class);

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private int jwtExpirationMs;

    @Value("${app.jwt.refresh-expiration-ms}")
    private int refreshExpirationMs;

    @Value("${app.cookie.access-token-max-age-seconds}")
    private int accessTokenDuration;

    @Value("${app.cookie.refresh-token-max-age-seconds}")
    private int refreshTokenDuration;

    @Value("${app.cookie.secure}")
    private boolean secure;

    @Value("${app.cookie.same-site}")
    private String sameSite;

    private Key signingKey;

    @PostConstruct
    public void init() {
        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    private Key key() {
        return signingKey;
    }

    // JWT LOGIC

    public String generateAccessToken(String username) {
        return generateTokenFromUsername(username, jwtExpirationMs);
    }

    public String generateRefreshToken(String username) {
        return generateTokenFromUsername(username, refreshExpirationMs);
    }

    private String generateTokenFromUsername(String username, int expiration) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + expiration))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parse(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            logger.debug("Token inválido: {}", e.getMessage());
        }
        return false;
    }

    // COOKIE LOGIC

    public ResponseCookie generateAccessTokenCookie(String token) {
        return generateCookie("access_token", token, accessTokenDuration);
    }

    public ResponseCookie generateRefreshTokenCookie(String token) {
        return generateCookie("refresh_token", token, refreshTokenDuration);
    }

    public ResponseCookie getCleanAccessTokenCookie() {
        return generateCookie("access_token", null, 0);
    }

    public ResponseCookie getCleanRefreshTokenCookie() {
        return generateCookie("refresh_token", null, 0);
    }

    private ResponseCookie generateCookie(String name, String value, int maxAge) {
        return ResponseCookie.from(name, value)
                .path("/")
                .maxAge(maxAge)
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .build();
    }
}
