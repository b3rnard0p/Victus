package com.example.sistemanutricao.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class FormValidationException extends ValidationException {
    public FormValidationException(String message) {
        super(message);
    }
}
