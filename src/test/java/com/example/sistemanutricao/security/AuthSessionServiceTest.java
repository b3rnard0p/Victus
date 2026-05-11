package com.example.sistemanutricao.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class AuthSessionServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private SecurityTokenManager tokenManager;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthSessionService authSessionService;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldAuthenticateAndWriteCookiesOnSuccessfulLogin() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        ResponseCookie accessCookie = ResponseCookie.from("access_token", "access").path("/").build();
        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", "refresh").path("/").build();

        when(authenticationManager.authenticate(new UsernamePasswordAuthenticationToken("ana@exemplo.com", "senha123")))
                .thenReturn(authentication);
        when(tokenManager.generateAccessToken("ana@exemplo.com")).thenReturn("access");
        when(tokenManager.generateRefreshToken("ana@exemplo.com")).thenReturn("refresh");
        when(tokenManager.generateAccessTokenCookie("access")).thenReturn(accessCookie);
        when(tokenManager.generateRefreshTokenCookie("refresh")).thenReturn(refreshCookie);

        LoginStatus status = authSessionService.login("ana@exemplo.com", "senha123", response);

        assertThat(status).isEqualTo(LoginStatus.SUCCESS);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(authentication);
        assertThat(response.getHeaders(HttpHeaders.SET_COOKIE))
                .containsExactly(accessCookie.toString(), refreshCookie.toString());
    }

    @Test
    void shouldReturnAccessDeniedWhenUserIsDisabled() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(authenticationManager.authenticate(new UsernamePasswordAuthenticationToken("ana@exemplo.com", "senha123")))
                .thenThrow(new DisabledException("disabled"));

        LoginStatus status = authSessionService.login("ana@exemplo.com", "senha123", response);

        assertThat(status).isEqualTo(LoginStatus.ACCESS_DENIED);
        assertThat(response.getHeaders(HttpHeaders.SET_COOKIE)).isEmpty();
    }

    @Test
    void shouldReturnInvalidCredentialsWhenAuthenticationFails() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(authenticationManager.authenticate(new UsernamePasswordAuthenticationToken("ana@exemplo.com", "senha123")))
                .thenThrow(new BadCredentialsException("bad credentials"));

        LoginStatus status = authSessionService.login("ana@exemplo.com", "senha123", response);

        assertThat(status).isEqualTo(LoginStatus.INVALID_CREDENTIALS);
        assertThat(response.getHeaders(HttpHeaders.SET_COOKIE)).isEmpty();
    }

    @Test
    void shouldClearCookiesAndSecurityContextOnLogout() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        ResponseCookie accessCookie = ResponseCookie.from("access_token", "").path("/").maxAge(0).build();
        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", "").path("/").maxAge(0).build();
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(tokenManager.getCleanAccessTokenCookie()).thenReturn(accessCookie);
        when(tokenManager.getCleanRefreshTokenCookie()).thenReturn(refreshCookie);

        authSessionService.logout(response);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(response.getHeaders(HttpHeaders.SET_COOKIE))
                .hasSize(2)
                .allSatisfy(cookie -> assertThat(cookie).contains("Path=/", "Max-Age=0"))
                .anySatisfy(cookie -> assertThat(cookie).contains("access_token="))
                .anySatisfy(cookie -> assertThat(cookie).contains("refresh_token="));
        verify(tokenManager).getCleanAccessTokenCookie();
        verify(tokenManager).getCleanRefreshTokenCookie();
    }
}
