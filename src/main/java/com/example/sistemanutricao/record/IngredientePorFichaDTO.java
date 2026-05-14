package com.example.sistemanutricao.record;

import java.math.BigDecimal;

import com.example.sistemanutricao.record.FichaTecnicaDTO.FichaTecnicaCreateDTO;
import com.example.sistemanutricao.record.IngredienteDTO.IngredienteGetDTO;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;

public record IngredientePorFichaDTO(
        Long id,
        Long ingredienteId,
        IngredienteGetDTO ingrediente,
        @Digits(integer = 4, fraction = 2, message = "O custo por kg deve ter no máximo 4 dígitos inteiros e 2 casas decimais.")
        BigDecimal custoKg,
        BigDecimal custoUsado,
        BigDecimal fc,
        @Size(max = 100, message = "A medida caseira deve ter no máximo 100 caracteres.")
        String medidaCaseira,
        @Digits(integer = 4, fraction = 2, message = "O PB deve ter no máximo 4 dígitos inteiros e 2 casas decimais.")
        BigDecimal pb,
        @Digits(integer = 4, fraction = 2, message = "O PL deve ter no máximo 4 dígitos inteiros e 2 casas decimais.")
        BigDecimal pl,
        BigDecimal ptnCalculado,
        BigDecimal choCalculado,
        BigDecimal lipCalculado,
        BigDecimal sodioCalculado,
        BigDecimal gorduraSaturadaCalculada,
        FichaTecnicaCreateDTO fichaTecnica
) {}

