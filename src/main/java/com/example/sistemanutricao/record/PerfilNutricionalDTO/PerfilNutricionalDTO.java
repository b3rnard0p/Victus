package com.example.sistemanutricao.record.PerfilNutricionalDTO;

import java.math.BigDecimal;

public record PerfilNutricionalDTO(
        Long id,
        BigDecimal vtc,
        BigDecimal kcalPtn,
        BigDecimal kcalCho,
        BigDecimal kcalLip,
        BigDecimal gramasPtn,
        BigDecimal gramasCho,
        BigDecimal gramasLip,
        BigDecimal gramasSodio,
        BigDecimal gramasSaturada,
        BigDecimal porcentPtn,
        BigDecimal porcentCho,
        BigDecimal porcentLip
) {}
