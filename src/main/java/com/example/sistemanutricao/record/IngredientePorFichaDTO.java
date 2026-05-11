package com.example.sistemanutricao.record;

import java.math.BigDecimal;

import com.example.sistemanutricao.record.FichaTecnicaDTO.FichaTecnicaCreateDTO;
import com.example.sistemanutricao.record.IngredienteDTO.IngredienteGetDTO;

public record IngredientePorFichaDTO(
        Long id,
        Long ingredienteId,
        IngredienteGetDTO ingrediente,
        BigDecimal custoKg,
        BigDecimal custoUsado,
        BigDecimal fc,
        String medidaCaseira,
        BigDecimal pb,
        BigDecimal pl,
        BigDecimal ptnCalculado,
        BigDecimal choCalculado,
        BigDecimal lipCalculado,
        BigDecimal sodioCalculado,
        BigDecimal gorduraSaturadaCalculada,
        FichaTecnicaCreateDTO fichaTecnica
) {}

