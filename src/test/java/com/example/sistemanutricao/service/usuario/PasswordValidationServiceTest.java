package com.example.sistemanutricao.service.usuario;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class PasswordValidationServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordValidationService service;

    @Test
    void shouldReturnEmptyWhenNovaSenhaIsNull() {
        Optional<String> result = service.validar(null, null, null, "hash");
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenNovaSenhaIsBlank() {
        Optional<String> result = service.validar(null, "", null, "hash");
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnErrorWhenSenhaAtualIncorreta() {
        when(passwordEncoder.matches(any(), anyString())).thenReturn(false);

        Optional<String> result = service.validar("senhaErrada", "nova123", "nova123", "hash");
        assertThat(result).contains("Senha atual incorreta");
    }

    @Test
    void shouldReturnErrorWhenSenhasNaoCoincidem() {
        when(passwordEncoder.matches(any(), anyString())).thenReturn(true);

        Optional<String> result = service.validar("certa", "nova123", "diferente", "hash");
        assertThat(result).contains("As novas senhas não coincidem");
    }

    @Test
    void shouldReturnEmptyWhenValidacaoPassar() {
        when(passwordEncoder.matches(any(), anyString())).thenReturn(true);

        Optional<String> result = service.validar("certa", "nova123", "nova123", "hash");
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnErrorWhenSenhaAtualIsNullAndNovaSenhaProvided() {
        Optional<String> result = service.validar(null, "nova123", "nova123", "hash");
        assertThat(result).contains("Senha atual incorreta");
    }
}
