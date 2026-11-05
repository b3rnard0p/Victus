package com.example.sistemanutricao.service.usuario;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordValidationService {

    public static final String SENHA_COMPLEXA_REGEX = "^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&*(),.?\":{}|<>_\\-]).{8,50}$";
    public static final String MENSAGEM_SENHA_FRACA = "A senha deve ter entre 8 e 50 caracteres e conter ao menos uma letra, um número e um caractere especial.";

    private final PasswordEncoder passwordEncoder;

    public PasswordValidationService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public static boolean isSenhaValida(String senha) {
        if (senha == null) return false;
        return senha.matches(SENHA_COMPLEXA_REGEX);
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

        if (!isSenhaValida(novaSenha)) {
            return Optional.of(MENSAGEM_SENHA_FRACA);
        }

        return Optional.empty();
    }
}
