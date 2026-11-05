package com.example.sistemanutricao.record.PreparacaoDTO;

import com.example.sistemanutricao.model.enuns.Categoria;
import java.math.BigDecimal;

public record PreparacaoGetDTO(
        Long id,
        String nome,
        Integer numero,
        String tempoPreparo,
        String equipamentos,
        String modoPreparo,
        BigDecimal porcentAgua,
        BigDecimal qntdAgua,
        BigDecimal fcc,
        BigDecimal rendimento,
        Categoria categoria
) {
    public String rendimentoFormatado() {
        return com.example.sistemanutricao.util.NumberFormatUtil.formatar(this.rendimento);
    }

    public String fccFormatado() {
        return com.example.sistemanutricao.util.NumberFormatUtil.formatar(this.fcc);
    }

    public String qntdAguaFormatado() {
        return com.example.sistemanutricao.util.NumberFormatUtil.formatar(this.qntdAgua);
    }

    public String porcentAguaFormatado() {
        return com.example.sistemanutricao.util.NumberFormatUtil.formatar(this.porcentAgua);
    }
}
