package com.example.sistemanutricao.record.IngredienteDTO;

import com.example.sistemanutricao.model.enuns.Status;

import java.math.BigDecimal;

public record IngredienteGetDTO(
        Long id,
        String nome,
        BigDecimal ptn,
        BigDecimal cho,
        BigDecimal lip,
        Status status,
        BigDecimal sodio,
        BigDecimal gorduraSaturada,
        Long usuarioId
) {}
