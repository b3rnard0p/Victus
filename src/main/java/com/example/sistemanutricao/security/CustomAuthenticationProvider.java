package com.example.sistemanutricao.security;

import com.example.sistemanutricao.exception.UsuarioSemCargoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationProvider.class);

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    public CustomAuthenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();
        
        logger.info("Tentando autenticar usuário: {}", username);
        
        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            
            if (!userDetails.isEnabled()) {
                logger.info("Usuário inativo: {}", username);
                throw new DisabledException("Usuário inativo");
            }
            
            if (passwordEncoder.matches(password, userDetails.getPassword())) {
                logger.info("Autenticação bem-sucedida para: {}", username);
                return new UsernamePasswordAuthenticationToken(userDetails, password, userDetails.getAuthorities());
            } else {
                logger.info("Senha incorreta para: {}", username);
                throw new BadCredentialsException("Senha incorreta");
            }
        } catch (UsuarioSemCargoException e) {
            logger.info("Usuário sem cargo: {}", username);
            throw e;
        } catch (DisabledException e) {
            throw e;
        } catch (Exception e) {
            logger.info("Erro na autenticação para {}: {}", username, e.getMessage());
            throw new BadCredentialsException("Usuário ou senha inválidos");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
} 