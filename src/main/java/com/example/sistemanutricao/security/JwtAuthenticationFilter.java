package com.example.sistemanutricao.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final SecurityTokenManager tokenManager;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(SecurityTokenManager tokenManager, UserDetailsService userDetailsService) {
        this.tokenManager = tokenManager;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        return path.startsWith("/output.css") ||
               path.startsWith("/Scripts/") ||
               path.startsWith("/imagens/") ||
               path.startsWith("/imagens-perfil/") ||
               path.startsWith("/uploads/") ||
               path.equals("/") ||
               path.equals("/login") ||
               path.equals("/registrar") ||
               path.startsWith("/favicon") ||
               path.startsWith("/actuator/health");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        Optional<String> accessTokenOpt = getCookieValueByName(request, "access_token");
        Optional<String> refreshTokenOpt = getCookieValueByName(request, "refresh_token");

        try {
            if (accessTokenOpt.isPresent() && tokenManager.validateJwtToken(accessTokenOpt.get())) {
                authenticateUser(accessTokenOpt.get(), request);
            }
            else if (refreshTokenOpt.isPresent() && tokenManager.validateJwtToken(refreshTokenOpt.get())) {
                String username = tokenManager.getUserNameFromJwtToken(refreshTokenOpt.get());

                String newAccessToken = tokenManager.generateAccessToken(username);

                response.addHeader(HttpHeaders.SET_COOKIE, tokenManager.generateAccessTokenCookie(newAccessToken).toString());

                authenticateUser(newAccessToken, request);
            }
        } catch (Exception e) {
            logger.debug("Não foi possível definir a autenticação do usuário: {}", e);
        }

        filterChain.doFilter(request, response);
    }

    private void authenticateUser(String token, HttpServletRequest request) {
        String username = tokenManager.getUserNameFromJwtToken(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private Optional<String> getCookieValueByName(HttpServletRequest request, String name) {
        if (request.getCookies() != null) {
            return Arrays.stream(request.getCookies())
                    .filter(cookie -> cookie.getName().equals(name))
                    .map(Cookie::getValue)
                    .findFirst();
        }
        return Optional.empty();
    }
}