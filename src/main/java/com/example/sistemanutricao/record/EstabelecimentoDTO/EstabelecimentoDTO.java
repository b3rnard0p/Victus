package com.example.sistemanutricao.record.EstabelecimentoDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EstabelecimentoDTO(
        Long id,
        @NotBlank(message = "O nome do estabelecimento é obrigatório.")
        @Size(max = 100, message = "O nome pode ter no máximo 100 caracteres.")
        String nome
) {}
