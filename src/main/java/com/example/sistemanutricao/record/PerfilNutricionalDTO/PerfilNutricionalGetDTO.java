package com.example.sistemanutricao.record.PerfilNutricionalDTO;

import java.math.BigDecimal;

public record PerfilNutricionalGetDTO(
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
) {
    public String gramasPTNFormatado() {
        return com.example.sistemanutricao.util.NumberFormatUtil.formatar(this.gramasPTN);
    }

    public String gramasCHOFormatado() {
        return com.example.sistemanutricao.util.NumberFormatUtil.formatar(this.gramasCHO);
    }

    public String gramasLIPFormatado() {
        return com.example.sistemanutricao.util.NumberFormatUtil.formatar(this.gramasLIP);
    }

    public String gramasSodioFormatado() {
        return com.example.sistemanutricao.util.NumberFormatUtil.formatar(this.gramasSodio);
    }

    public String gramasSaturadaFormatado() {
        return com.example.sistemanutricao.util.NumberFormatUtil.formatar(this.gramasSaturada);
    }

    public String vtcFormatado() {
        return com.example.sistemanutricao.util.NumberFormatUtil.formatar(this.vtc);
    }
}