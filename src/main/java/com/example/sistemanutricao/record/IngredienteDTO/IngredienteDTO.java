package com.example.sistemanutricao.record.IngredienteDTO;

import com.example.sistemanutricao.model.enuns.Status;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record IngredienteDTO(
        Long id,
        @NotBlank(message = "O nome é obrigatório.")
        @Size(max = 100, message = "O nome pode ter no máximo 100 caracteres.")
        String nome,
        @NotNull(message = "A proteína é obrigatória.")
        @DecimalMax(value = "100", message = "A proteína deve ser menor ou igual a 100.")
        BigDecimal ptn,
        @NotNull(message = "O Carboidrato é obrigatório.")
        @DecimalMax(value = "100", message = "O carboidrato deve ser menor ou igual a 100.")
        BigDecimal cho,
        @NotNull(message = "O lipídios é obrigatório.")
        @DecimalMax(value = "100", message = "O lipídio deve ser menor ou igual a 100.")
        BigDecimal lip,
        @NotNull(message = "O sódio é obrigatório.")
        @DecimalMax(value = "100", message = "O sódio deve ser menor ou igual a 100.")
        BigDecimal sodio,
        Status status,
        @DecimalMax(value = "100", message = "A gordura saturada deve ser menor ou igual a 100.")
        BigDecimal gorduraSaturada,
        Long usuarioId
) {}
