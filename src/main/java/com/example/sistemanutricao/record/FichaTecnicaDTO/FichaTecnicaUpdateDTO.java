package com.example.sistemanutricao.record.FichaTecnicaDTO;

import com.example.sistemanutricao.model.enuns.Status;
import com.example.sistemanutricao.model.enuns.StatusCriacao;
import com.example.sistemanutricao.record.IngredientePorFichaDTO;
import com.example.sistemanutricao.record.PerfilNutricionalDTO.PerfilNutricionalDTO;
import com.example.sistemanutricao.record.PreparacaoDTO.PreparacaoDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record FichaTecnicaUpdateDTO(
        Long id,
        BigDecimal custoPerCapita,
        @NotNull(message = "O custo total é obrigatório.")
        BigDecimal custoTotal,
        @NotBlank(message = "A medida caseira deve ser informada.")
        @Size(max = 100, message = "A medida caseira deve ter no máximo 100 caracteres.")
        String medidaCaseira,
        @Max(value = 9999, message = "O número de porções deve ter no máximo 4 dígitos.")
        Integer numeroPorcoes,
        @NotNull(message = "O peso da porção é obrigatório.")
        @DecimalMin(value = "0.1", message = "O peso da porção deve ser maior que zero.")
        @Digits(integer = 4, fraction = 2, message = "O peso da porção deve ter no máximo 4 dígitos inteiros e 2 casas decimais.")
        BigDecimal pesoPorcao,
        Status status,
        StatusCriacao statusCriacao,
        @Valid
        PreparacaoDTO preparacao,
        @NotEmpty(message = "A ficha deve conter pelo menos um ingrediente.")
        List<IngredientePorFichaDTO> ingredientes,
        PerfilNutricionalDTO perfilNutricional
) {}
