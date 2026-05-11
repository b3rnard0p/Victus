package com.example.sistemanutricao.record.RefeicaoDTO;

import com.example.sistemanutricao.model.enuns.Status;

import java.util.List;

public record RefeicaoResponseDTO(Long id, String nome, String kcalTotal, Status status, List<FichaTecnicaRefeicaoDTO> fichasTecnicas) {
}
