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

        Optional<String> result = service.validar("senhaErrada", "Nova@1234", "Nova@1234", "hash");
        assertThat(result).contains("Senha atual incorreta");
    }

    @Test
    void shouldReturnErrorWhenSenhasNaoCoincidem() {
        when(passwordEncoder.matches(any(), anyString())).thenReturn(true);

        Optional<String> result = service.validar("certa", "Nova@1234", "diferente", "hash");
        assertThat(result).contains("As novas senhas não coincidem");
    }

    @Test
    void shouldReturnEmptyWhenValidacaoPassar() {
        when(passwordEncoder.matches(any(), anyString())).thenReturn(true);

        Optional<String> result = service.validar("certa", "Nova@1234", "Nova@1234", "hash");
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnErrorWhenNovaSenhaIsTooShort() {
        when(passwordEncoder.matches(any(), anyString())).thenReturn(true);

        Optional<String> result = service.validar("certa", "123", "123", "hash");
        assertThat(result).contains(PasswordValidationService.MENSAGEM_SENHA_FRACA);
    }

    @Test
    void shouldReturnErrorWhenNovaSenhaLacksSpecialChar() {
        when(passwordEncoder.matches(any(), anyString())).thenReturn(true);

        Optional<String> result = service.validar("certa", "SenhaForte123", "SenhaForte123", "hash");
        assertThat(result).contains(PasswordValidationService.MENSAGEM_SENHA_FRACA);
    }

    @Test
    void shouldReturnErrorWhenNovaSenhaLacksNumber() {
        when(passwordEncoder.matches(any(), anyString())).thenReturn(true);

        Optional<String> result = service.validar("certa", "SenhaForte@", "SenhaForte@", "hash");
        assertThat(result).contains(PasswordValidationService.MENSAGEM_SENHA_FRACA);
    }

    @Test
    void shouldReturnErrorWhenNovaSenhaLacksLetter() {
        when(passwordEncoder.matches(any(), anyString())).thenReturn(true);

        Optional<String> result = service.validar("certa", "12345678@", "12345678@", "hash");
        assertThat(result).contains(PasswordValidationService.MENSAGEM_SENHA_FRACA);
    }

    @Test
    void shouldReturnErrorWhenSenhaAtualIsNullAndNovaSenhaProvided() {
        Optional<String> result = service.validar(null, "Nova@1234", "Nova@1234", "hash");
        assertThat(result).contains("Senha atual incorreta");
    }
}
