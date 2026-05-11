package com.example.sistemanutricao.exception;

public class EntityNotFoundException extends DomainException {
    
    public EntityNotFoundException(String message) {
        super(message);
    }
    
    public EntityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
