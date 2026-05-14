package com.example.sistemanutricao.record.RefeicaoDTO;

import com.example.sistemanutricao.model.enuns.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record RefeicaoDTO(
        @NotBlank(message = "O nome da refeição é obrigatório.")
        @Size(max = 100, message = "O nome da refeição deve ter no máximo 100 caracteres.")
        String nome,
        
        String kcalTotal,
        
        Status status,
        
        @NotEmpty(message = "A refeição deve ter pelo menos uma ficha técnica selecionada.")
        List<Long> fichasTecnicasIds
) {}