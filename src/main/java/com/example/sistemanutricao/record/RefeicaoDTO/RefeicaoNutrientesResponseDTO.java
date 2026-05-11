package com.example.sistemanutricao.record.RefeicaoDTO;

import java.math.BigDecimal;
import java.util.List;

import com.example.sistemanutricao.model.enuns.Status;

public record RefeicaoNutrientesResponseDTO(
    Long id,
    String nome,
    String kcalTotal,
    Status status,
    List<FichaTecnicaRefeicaoDTO> fichasTecnicas,
    BigDecimal totalGramasPTN,
    BigDecimal totalGramasCHO,
    BigDecimal totalGramasLIP,
    BigDecimal totalGramasSodio,
    BigDecimal totalGramasSaturada
) {}
