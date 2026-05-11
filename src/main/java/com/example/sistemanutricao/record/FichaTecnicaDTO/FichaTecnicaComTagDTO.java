package com.example.sistemanutricao.record.FichaTecnicaDTO;

import java.math.BigDecimal;

import com.example.sistemanutricao.model.enuns.Status;
import com.example.sistemanutricao.model.enuns.StatusCriacao;

public record FichaTecnicaComTagDTO(
    Long id,
    String nomePreparacao,
    String categoriaPreparacao,
    Integer numeroPreparacao,
    BigDecimal custoPerCapita,
    BigDecimal custoTotal,
    BigDecimal rendimento,
    BigDecimal vtc,
    BigDecimal gramasPTN,
    BigDecimal gramasCHO,
    BigDecimal gramasLIP,
    BigDecimal gramasSodio,
    BigDecimal gramasSaturada,
    Status status,
    StatusCriacao statusCriacao,
    Long nutricionistaId,
    String tag
) {
} 