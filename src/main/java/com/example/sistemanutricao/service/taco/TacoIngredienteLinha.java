package com.example.sistemanutricao.service.taco;

import java.math.BigDecimal;

public record TacoIngredienteLinha(
        String nome,
        BigDecimal ptn,
        BigDecimal cho,
        BigDecimal lip,
        BigDecimal sodio
) {
}
