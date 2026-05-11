package com.example.sistemanutricao.security;

import com.example.sistemanutricao.exception.UsuarioSemCargoException;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;

@Service
public class AuthSessionService {

    private final AuthenticationManager authenticationManager;
    private final SecurityTokenManager tokenManager;

    public AuthSessionService(AuthenticationManager authenticationManager,
                              SecurityTokenManager tokenManager) {
        this.authenticationManager = authenticationManager;
        this.tokenManager = tokenManager;
    }

    public LoginStatus login(String email, String password, HttpServletResponse response) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String accessToken = tokenManager.generateAccessToken(email);
            String refreshToken = tokenManager.generateRefreshToken(email);

            response.addHeader(HttpHeaders.SET_COOKIE, tokenManager.generateAccessTokenCookie(accessToken).toString());
            response.addHeader(HttpHeaders.SET_COOKIE, tokenManager.generateRefreshTokenCookie(refreshToken).toString());
            return LoginStatus.SUCCESS;
        } catch (AuthenticationException exception) {
            if (exception instanceof DisabledException || exception instanceof UsuarioSemCargoException) {
                return LoginStatus.ACCESS_DENIED;
            }
            if (exception instanceof BadCredentialsException) {
                return LoginStatus.INVALID_CREDENTIALS;
            }
            return LoginStatus.INVALID_CREDENTIALS;
        }
    }

    public void logout(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, tokenManager.getCleanAccessTokenCookie().toString());
        response.addHeader(HttpHeaders.SET_COOKIE, tokenManager.getCleanRefreshTokenCookie().toString());
        SecurityContextHolder.clearContext();
    }
}
