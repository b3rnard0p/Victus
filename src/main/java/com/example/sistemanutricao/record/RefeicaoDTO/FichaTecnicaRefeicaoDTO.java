package com.example.sistemanutricao.record.RefeicaoDTO;

import java.math.BigDecimal;

public record FichaTecnicaRefeicaoDTO(
        Long id, String nomePreparacao, BigDecimal vct
) {
}
