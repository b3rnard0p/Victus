package com.example.sistemanutricao.record.FichaTecnicaDTO;

import com.example.sistemanutricao.model.enuns.Status;
import com.example.sistemanutricao.model.enuns.StatusCriacao;
import com.example.sistemanutricao.record.IngredientePorFichaDTO;
import com.example.sistemanutricao.record.PerfilNutricionalDTO.PerfilNutricionalDTO;
import com.example.sistemanutricao.record.PreparacaoDTO.PreparacaoDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record FichaTecnicaUpdateDTO(
        Long id,
        BigDecimal custoPerCapita,
        @NotNull(message = "O custo total é obrigatório.")
        BigDecimal custoTotal,
        @NotBlank(message = "A medida caseira deve ser informada.")
        String medidaCaseira,
        Integer numeroPorcoes,
        @NotNull(message = "O peso da porção é obrigatório.")
        @DecimalMin(value = "0.1", message = "O peso da porção deve ser maior que zero.")
        BigDecimal pesoPorcao,
        Status status,
        StatusCriacao statusCriacao,
        @Valid
        PreparacaoDTO preparacao,
        @NotEmpty(message = "A ficha deve conter pelo menos um ingrediente.")
        List<IngredientePorFichaDTO> ingredientes,
        PerfilNutricionalDTO perfilNutricional
) {}
