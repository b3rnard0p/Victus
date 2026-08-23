package com.example.sistemanutricao.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseCookie;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class SecurityTokenManagerTest {

    @InjectMocks
    private SecurityTokenManager securityTokenManager;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(securityTokenManager, "jwtSecret", "mySecretKeyForTestingThatIsLongEnoughToNotThrowException");
        ReflectionTestUtils.setField(securityTokenManager, "jwtExpirationMs", 3600000); // 1 hour
        ReflectionTestUtils.setField(securityTokenManager, "refreshExpirationMs", 86400000); // 1 day
        ReflectionTestUtils.setField(securityTokenManager, "accessTokenDuration", 3600); // 1 hour
        ReflectionTestUtils.setField(securityTokenManager, "refreshTokenDuration", 86400); // 1 day
        ReflectionTestUtils.setField(securityTokenManager, "secure", false);
        ReflectionTestUtils.setField(securityTokenManager, "sameSite", "Lax");
        securityTokenManager.init();
    }

    @Test
    void shouldGenerateTokensWithDefaultExpiration() {
        String accessToken = securityTokenManager.generateAccessToken("usuario@exemplo.com", false);
        String refreshToken = securityTokenManager.generateRefreshToken("usuario@exemplo.com", false);

        assertThat(accessToken).isNotNull();
        assertThat(refreshToken).isNotNull();
        
        assertThat(securityTokenManager.getUserNameFromJwtToken(accessToken)).isEqualTo("usuario@exemplo.com");
    }

    @Test
    void shouldGenerateCookiesWithDefaultDuration() {
        ResponseCookie accessCookie = securityTokenManager.generateAccessTokenCookie("dummy_token", false);
        ResponseCookie refreshCookie = securityTokenManager.generateRefreshTokenCookie("dummy_token", false);

        assertThat(accessCookie.getMaxAge().getSeconds()).isEqualTo(3600);
        assertThat(refreshCookie.getMaxAge().getSeconds()).isEqualTo(86400);
    }

    @Test
    void shouldGenerateCookiesWithRememberMeDuration() {
        ResponseCookie accessCookie = securityTokenManager.generateAccessTokenCookie("dummy_token", true);
        ResponseCookie refreshCookie = securityTokenManager.generateRefreshTokenCookie("dummy_token", true);

        long thirtyDaysInSeconds = 30L * 24 * 60 * 60;
        assertThat(accessCookie.getMaxAge().getSeconds()).isEqualTo(thirtyDaysInSeconds);
        assertThat(refreshCookie.getMaxAge().getSeconds()).isEqualTo(thirtyDaysInSeconds);
    }
}
