package com.example.sistemanutricao.record;

import java.math.BigDecimal;

import com.example.sistemanutricao.record.FichaTecnicaDTO.FichaTecnicaCreateDTO;
import com.example.sistemanutricao.record.IngredienteDTO.IngredienteGetDTO;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;

public record IngredientePorFichaDTO(
        Long id,
        Long ingredienteId,
        IngredienteGetDTO ingrediente,
        @Digits(integer = 10, fraction = 5, message = "O custo por kg deve ter no máximo 10 dígitos inteiros e 5 casas decimais.")
        BigDecimal custoKg,
        BigDecimal custoUsado,
        BigDecimal fc,
        @Size(max = 100, message = "A medida caseira deve ter no máximo 100 caracteres.")
        String medidaCaseira,
        @Digits(integer = 10, fraction = 5, message = "O PB deve ter no máximo 10 dígitos inteiros e 5 casas decimais.")
        BigDecimal pb,
        @Digits(integer = 10, fraction = 5, message = "O PL deve ter no máximo 10 dígitos inteiros e 5 casas decimais.")
        BigDecimal pl,
        BigDecimal ptnCalculado,
        BigDecimal choCalculado,
        BigDecimal lipCalculado,
        BigDecimal sodioCalculado,
        BigDecimal gorduraSaturadaCalculada,
        FichaTecnicaCreateDTO fichaTecnica
) {
    public String pbFormatado() {
        return com.example.sistemanutricao.util.NumberFormatUtil.formatar(this.pb);
    }

    public String plFormatado() {
        return com.example.sistemanutricao.util.NumberFormatUtil.formatar(this.pl);
    }

    public String fcFormatado() {
        return com.example.sistemanutricao.util.NumberFormatUtil.formatar(this.fc);
    }
}

