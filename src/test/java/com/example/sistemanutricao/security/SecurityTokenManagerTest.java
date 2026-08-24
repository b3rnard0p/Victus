package com.example.sistemanutricao.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class SecurityTokenManagerTest {

    private SecurityTokenManager tokenManager;

    @BeforeEach
    void setUp() {
        tokenManager = new SecurityTokenManager();
        
        // Inject values
        ReflectionTestUtils.setField(tokenManager, "jwtSecret", "mySecretKeyForTestingThatIsLongEnoughToUseWithHmacSha256");
        ReflectionTestUtils.setField(tokenManager, "jwtExpirationMs", 3600000);
        ReflectionTestUtils.setField(tokenManager, "refreshExpirationMs", 7200000);
        ReflectionTestUtils.setField(tokenManager, "accessTokenDuration", 3600);
        ReflectionTestUtils.setField(tokenManager, "refreshTokenDuration", 7200);
        ReflectionTestUtils.setField(tokenManager, "secure", true);
        ReflectionTestUtils.setField(tokenManager, "sameSite", "Strict");
        
        tokenManager.init();
    }

    @Test
    @DisplayName("Deve gerar e validar access token")
    void deveGerarEValidarAccessToken() {
        String token = tokenManager.generateAccessToken("testeuser");
        
        assertNotNull(token);
        assertTrue(tokenManager.validateJwtToken(token));
        assertEquals("testeuser", tokenManager.getUserNameFromJwtToken(token));
    }

    @Test
    @DisplayName("Deve gerar access token com remember me")
    void deveGerarAccessTokenRememberMe() {
        String token = tokenManager.generateAccessToken("testeuser", true);
        
        assertNotNull(token);
        assertTrue(tokenManager.validateJwtToken(token));
    }

    @Test
    @DisplayName("Deve gerar refresh token")
    void deveGerarRefreshToken() {
        String token = tokenManager.generateRefreshToken("testeuser");
        
        assertNotNull(token);
        assertTrue(tokenManager.validateJwtToken(token));
    }

    @Test
    @DisplayName("Deve gerar refresh token com remember me")
    void deveGerarRefreshTokenRememberMe() {
        String token = tokenManager.generateRefreshToken("testeuser", true);
        
        assertNotNull(token);
        assertTrue(tokenManager.validateJwtToken(token));
    }

    @Test
    @DisplayName("Nao deve validar token expirado ou malformado")
    void naoDeveValidarTokenMalformado() {
        assertFalse(tokenManager.validateJwtToken("token-invalido"));
        
        String wrongSecretToken = Jwts.builder()
                .setSubject("teste")
                .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor("anotherSecretKeyForTestingThatIsLongEnoughToUseWithHmacSha256".getBytes()), SignatureAlgorithm.HS256)
                .compact();
                
        assertFalse(tokenManager.validateJwtToken(wrongSecretToken));
    }

    @Test
    @DisplayName("Deve gerar access token cookie")
    void deveGerarAccessTokenCookie() {
        ResponseCookie cookie = tokenManager.generateAccessTokenCookie("mytoken");
        
        assertEquals("access_token", cookie.getName());
        assertEquals("mytoken", cookie.getValue());
        assertEquals(3600, cookie.getMaxAge().getSeconds());
        assertTrue(cookie.isHttpOnly());
        assertTrue(cookie.isSecure());
        assertEquals("Strict", cookie.getSameSite());
    }

    @Test
    @DisplayName("Deve gerar access token cookie com remember me")
    void deveGerarAccessTokenCookieRememberMe() {
        ResponseCookie cookie = tokenManager.generateAccessTokenCookie("mytoken", true);
        
        assertEquals("access_token", cookie.getName());
        assertEquals(30 * 24 * 60 * 60, cookie.getMaxAge().getSeconds());
    }

    @Test
    @DisplayName("Deve gerar refresh token cookie")
    void deveGerarRefreshTokenCookie() {
        ResponseCookie cookie = tokenManager.generateRefreshTokenCookie("mytoken");
        
        assertEquals("refresh_token", cookie.getName());
        assertEquals(7200, cookie.getMaxAge().getSeconds());
    }

    @Test
    @DisplayName("Deve gerar refresh token cookie com remember me")
    void deveGerarRefreshTokenCookieRememberMe() {
        ResponseCookie cookie = tokenManager.generateRefreshTokenCookie("mytoken", true);
        
        assertEquals("refresh_token", cookie.getName());
        assertEquals(30 * 24 * 60 * 60, cookie.getMaxAge().getSeconds());
    }

    @Test
    @DisplayName("Deve limpar access token cookie")
    void deveLimparAccessTokenCookie() {
        ResponseCookie cookie = tokenManager.getCleanAccessTokenCookie();
        
        assertEquals("access_token", cookie.getName());
        assertEquals("", cookie.getValue());
        assertEquals(0, cookie.getMaxAge().getSeconds());
    }

    @Test
    @DisplayName("Deve limpar refresh token cookie")
    void deveLimparRefreshTokenCookie() {
        ResponseCookie cookie = tokenManager.getCleanRefreshTokenCookie();
        
        assertEquals("refresh_token", cookie.getName());
        assertEquals("", cookie.getValue());
        assertEquals(0, cookie.getMaxAge().getSeconds());
    }
}
