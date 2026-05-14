package com.example.sistemanutricao.record.PerfilNutricionalDTO;

import java.math.BigDecimal;

public record PerfilNutricionalDTO(
        Long id,
        BigDecimal vtc,
        BigDecimal kcalPTN,
        BigDecimal kcalCHO,
        BigDecimal kcalLIP,
        BigDecimal gramasPTN,
        BigDecimal gramasCHO,
        BigDecimal gramasLIP,
        BigDecimal gramasSodio,
        BigDecimal gramasSaturada,
        BigDecimal porcentPTN,
        BigDecimal porcentCHO,
        BigDecimal porcentLIP
) {}
