package com.example.sistemanutricao.record.FichaTecnicaDTO;

import com.example.sistemanutricao.model.enuns.Status;
import com.example.sistemanutricao.model.enuns.StatusCriacao;
import com.example.sistemanutricao.record.IngredientePorFichaDTO;
import com.example.sistemanutricao.record.PerfilNutricionalDTO.PerfilNutricionalGetDTO;
import com.example.sistemanutricao.record.PreparacaoDTO.PreparacaoGetDTO;

import java.math.BigDecimal;
import java.util.List;

public record FichaTecnicaGetDTO(
        Long id,
        BigDecimal custoPerCapita,
        BigDecimal custoTotal,
        String medidaCaseira,
        Integer numeroPorcoes,
        BigDecimal pesoPorcao,
        Status status,
        StatusCriacao statusCriacao,
        PreparacaoGetDTO preparacao,
        List<IngredientePorFichaDTO> ingredientes,
        PerfilNutricionalGetDTO perfilNutricional
) {}

