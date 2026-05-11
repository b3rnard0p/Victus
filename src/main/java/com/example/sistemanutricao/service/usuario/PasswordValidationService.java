package com.example.sistemanutricao.service.usuario;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordValidationService {

    private final PasswordEncoder passwordEncoder;

    public PasswordValidationService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<String> validar(String senhaAtual, String novaSenha,
                                    String confirmarNovaSenha, String hashAtual) {
        if (novaSenha == null || novaSenha.isEmpty()) {
            return Optional.empty();
        }

        if (senhaAtual == null || !passwordEncoder.matches(senhaAtual, hashAtual)) {
            return Optional.of("Senha atual incorreta");
        }

        if (!novaSenha.equals(confirmarNovaSenha)) {
            return Optional.of("As novas senhas não coincidem");
        }

        return Optional.empty();
    }
}
