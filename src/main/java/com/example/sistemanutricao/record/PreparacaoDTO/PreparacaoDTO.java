package com.example.sistemanutricao.record.PreparacaoDTO;

import com.example.sistemanutricao.model.enuns.Categoria;
import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PreparacaoDTO(
        Long id,
        @NotBlank(message = "O nome da preparação é obrigatório.")
        String nome,
        Integer numero,
        @NotBlank(message = "O tempo de preparo é obrigatório.")
        String tempoPreparo,
        @NotBlank(message = "Os equipamentos são obrigatórios.")
        String equipamentos,
        @NotBlank(message = "O modo de preparo é obrigatório.")
        String modoPreparo,
        BigDecimal porcentAgua,
        BigDecimal qntdAgua,
        @NotNull(message = "O FCC é obrigatório.")
        BigDecimal fcc,
        BigDecimal rendimento,
        @NotNull(message = "A categoria é obrigatória.")
        Categoria categoria
) {}
