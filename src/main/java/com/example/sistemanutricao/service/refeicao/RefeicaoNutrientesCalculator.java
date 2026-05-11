package com.example.sistemanutricao.service.refeicao;

import java.math.BigDecimal;
import java.util.Objects;

import com.example.sistemanutricao.model.FichasPorRefeicao;
import com.example.sistemanutricao.model.PerfilNutricional;
import com.example.sistemanutricao.model.Refeicao;

public class RefeicaoNutrientesCalculator {

    public record Totais(
            BigDecimal totalGramasPTN,
            BigDecimal totalGramasCHO,
            BigDecimal totalGramasLIP,
            BigDecimal totalGramasSodio,
            BigDecimal totalGramasSaturada) {}

    public Totais calcularTotais(Refeicao refeicao) {
        BigDecimal totalGramasPTN = BigDecimal.ZERO;
        BigDecimal totalGramasCHO = BigDecimal.ZERO;
        BigDecimal totalGramasLIP = BigDecimal.ZERO;
        BigDecimal totalGramasSodio = BigDecimal.ZERO;
        BigDecimal totalGramasSaturada = BigDecimal.ZERO;

        if (refeicao == null || refeicao.getFichasPorRefeicao() == null) {
            return new Totais(totalGramasPTN, totalGramasCHO, totalGramasLIP, totalGramasSodio, totalGramasSaturada);
        }

        for (FichasPorRefeicao fp : refeicao.getFichasPorRefeicao()) {
            PerfilNutricional pn = fp.getFichaTecnica().getPerfilNutricional();
            if (pn != null) {
                totalGramasPTN = totalGramasPTN.add(Objects.requireNonNullElse(pn.getGramasPTN(), BigDecimal.ZERO));
                totalGramasCHO = totalGramasCHO.add(Objects.requireNonNullElse(pn.getGramasCHO(), BigDecimal.ZERO));
                totalGramasLIP = totalGramasLIP.add(Objects.requireNonNullElse(pn.getGramasLIP(), BigDecimal.ZERO));
                totalGramasSodio = totalGramasSodio.add(Objects.requireNonNullElse(pn.getGramasSodio(), BigDecimal.ZERO));
                totalGramasSaturada = totalGramasSaturada.add(Objects.requireNonNullElse(pn.getGramasSaturada(), BigDecimal.ZERO));
            }
        }

        return new Totais(totalGramasPTN, totalGramasCHO, totalGramasLIP, totalGramasSodio, totalGramasSaturada);
    }
}
