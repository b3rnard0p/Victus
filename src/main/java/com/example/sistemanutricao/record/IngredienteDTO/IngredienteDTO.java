package com.example.sistemanutricao.record.IngredienteDTO;

import com.example.sistemanutricao.model.enuns.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record IngredienteDTO(
        Long id,
        @NotBlank(message = "O nome é obrigatório.")
        String nome,
        @NotNull(message = "A proteína é obrigatória.")
        BigDecimal ptn,
        @NotNull(message = "O Carboidrato é obrigatório.")
        BigDecimal cho,
        @NotNull(message = "O lipídios é obrigatório.")
        BigDecimal lip,
        @NotNull(message = "O sódio é obrigatório.")
        BigDecimal sodio,
        Status status,
        BigDecimal gorduraSaturada,
        Long usuarioId
) {}
