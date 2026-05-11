package com.example.sistemanutricao.record.IngredienteDTO;

import java.math.BigDecimal;

import com.example.sistemanutricao.model.enuns.Status;

public record IngredienteComTagDTO(
    Long id,
    String nome,
    BigDecimal ptn,
    BigDecimal cho,
    BigDecimal lip,
    Status status,
    BigDecimal sodio,
    BigDecimal gorduraSaturada,
    Long usuarioId,
    String tag
) {
} 