package com.example.sistemanutricao.record.UsuarioDTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioDTO(
        @NotBlank(message = "O nome de usuário é obrigatório.")
        @Size(min = 3, max = 50, message = "O nome de usuário deve ter entre 3 e 50 caracteres.")
        String username,

        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "O e-mail deve ser válido.")
        String email,

        String senha,
        String novaSenha,
        String confirmarNovaSenha,
        String senhaAtual,
        String caminhoImagem
) {}
