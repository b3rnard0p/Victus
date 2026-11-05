package com.example.sistemanutricao.security;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class RateLimitServiceTest {

    @Test
    void devePermitirAteDezTentativasEBloquearADecimaPrimeira() {
        RateLimitService service = new RateLimitService();

        for (int i = 0; i < 10; i++) {
            assertThatCode(() -> service.verificarLimite("login:user@test.com")).doesNotThrowAnyException();
        }

        assertThatThrownBy(() -> service.verificarLimite("login:user@test.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Muitas tentativas");
    }
}

