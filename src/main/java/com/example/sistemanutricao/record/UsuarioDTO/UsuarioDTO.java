package com.example.sistemanutricao.record.UsuarioDTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioDTO(
        @NotBlank(message = "O nome de usuário é obrigatório.")
        @Size(max = 100, message = "O nome de usuário deve ter no máximo 100 caracteres.")
        String username,

        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "O e-mail deve ser válido.")
        @Size(max = 100, message = "O e-mail deve ter no máximo 100 caracteres.")
        String email,

        @Size(max = 50, message = "A senha deve ter no máximo 50 caracteres.")
        String senha,
        String novaSenha,
        String confirmarNovaSenha,
        String senhaAtual,
        String caminhoImagem
) {}
