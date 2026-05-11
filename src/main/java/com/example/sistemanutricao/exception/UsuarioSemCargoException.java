package com.example.sistemanutricao.exception;

import org.springframework.security.core.AuthenticationException;

public class UsuarioSemCargoException extends AuthenticationException {
    
    public UsuarioSemCargoException(String msg) {
        super(msg);
    }
} 