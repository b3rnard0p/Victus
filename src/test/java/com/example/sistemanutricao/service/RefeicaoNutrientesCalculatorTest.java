package com.example.sistemanutricao.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.sistemanutricao.model.FichaTecnica;
import com.example.sistemanutricao.model.FichasPorRefeicao;
import com.example.sistemanutricao.model.PerfilNutricional;
import com.example.sistemanutricao.model.Refeicao;
import com.example.sistemanutricao.service.refeicao.RefeicaoNutrientesCalculator;

class RefeicaoNutrientesCalculatorTest {

    private final RefeicaoNutrientesCalculator calculator = new RefeicaoNutrientesCalculator();

    @Test
    void shouldReturnZerosWhenRefeicaoIsNull() {
        RefeicaoNutrientesCalculator.Totais totais = calculator.calcularTotais(null);

        assertThat(totais.totalGramasPTN()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(totais.totalGramasCHO()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(totais.totalGramasLIP()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(totais.totalGramasSodio()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(totais.totalGramasSaturada()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldReturnZerosWhenFichasPorRefeicaoIsNull() {
        Refeicao refeicao = new Refeicao();
        refeicao.setFichasPorRefeicao(null);

        RefeicaoNutrientesCalculator.Totais totais = calculator.calcularTotais(refeicao);

        assertThat(totais.totalGramasPTN()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldReturnZerosWhenFichasPorRefeicaoIsEmpty() {
        Refeicao refeicao = new Refeicao();
        refeicao.setFichasPorRefeicao(new ArrayList<>());

        RefeicaoNutrientesCalculator.Totais totais = calculator.calcularTotais(refeicao);

        assertThat(totais.totalGramasPTN()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(totais.totalGramasCHO()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldSumNutrientsFromMultipleFichas() {
        PerfilNutricional pn1 = new PerfilNutricional();
        pn1.setGramasPTN(new BigDecimal("10.00"));
        pn1.setGramasCHO(new BigDecimal("20.00"));
        pn1.setGramasLIP(new BigDecimal("5.00"));
        pn1.setGramasSodio(new BigDecimal("1.50"));
        pn1.setGramasSaturada(new BigDecimal("2.00"));

        PerfilNutricional pn2 = new PerfilNutricional();
        pn2.setGramasPTN(new BigDecimal("15.00"));
        pn2.setGramasCHO(new BigDecimal("30.00"));
        pn2.setGramasLIP(new BigDecimal("8.00"));
        pn2.setGramasSodio(new BigDecimal("2.50"));
        pn2.setGramasSaturada(new BigDecimal("3.00"));

        Refeicao refeicao = new Refeicao();
        refeicao.setFichasPorRefeicao(List.of(
            criarFichasPorRefeicao(pn1),
            criarFichasPorRefeicao(pn2)
        ));

        RefeicaoNutrientesCalculator.Totais totais = calculator.calcularTotais(refeicao);

        assertThat(totais.totalGramasPTN()).isEqualByComparingTo("25.00");
        assertThat(totais.totalGramasCHO()).isEqualByComparingTo("50.00");
        assertThat(totais.totalGramasLIP()).isEqualByComparingTo("13.00");
        assertThat(totais.totalGramasSodio()).isEqualByComparingTo("4.00");
        assertThat(totais.totalGramasSaturada()).isEqualByComparingTo("5.00");
    }

    @Test
    void shouldHandleNullValuesInPerfilNutricional() {
        PerfilNutricional pn = new PerfilNutricional();
        pn.setGramasPTN(new BigDecimal("10.00"));
        // Leave other fields null

        Refeicao refeicao = new Refeicao();
        refeicao.setFichasPorRefeicao(List.of(criarFichasPorRefeicao(pn)));

        RefeicaoNutrientesCalculator.Totais totais = calculator.calcularTotais(refeicao);

        assertThat(totais.totalGramasPTN()).isEqualByComparingTo("10.00");
        assertThat(totais.totalGramasCHO()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(totais.totalGramasLIP()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(totais.totalGramasSodio()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(totais.totalGramasSaturada()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldSkipFichaWithNullPerfilNutricional() {
        PerfilNutricional pn = new PerfilNutricional();
        pn.setGramasPTN(new BigDecimal("10.00"));
        pn.setGramasCHO(new BigDecimal("20.00"));
        pn.setGramasLIP(new BigDecimal("5.00"));
        pn.setGramasSodio(new BigDecimal("1.00"));
        pn.setGramasSaturada(new BigDecimal("2.00"));

        FichaTecnica ftComPerfil = new FichaTecnica();
        ftComPerfil.setPerfilNutricional(pn);

        FichaTecnica ftSemPerfil = new FichaTecnica();
        ftSemPerfil.setPerfilNutricional(null);

        FichasPorRefeicao fp1 = new FichasPorRefeicao();
        fp1.setFichaTecnica(ftComPerfil);

        FichasPorRefeicao fp2 = new FichasPorRefeicao();
        fp2.setFichaTecnica(ftSemPerfil);

        Refeicao refeicao = new Refeicao();
        refeicao.setFichasPorRefeicao(List.of(fp1, fp2));

        RefeicaoNutrientesCalculator.Totais totais = calculator.calcularTotais(refeicao);

        assertThat(totais.totalGramasPTN()).isEqualByComparingTo("10.00");
    }

    private FichasPorRefeicao criarFichasPorRefeicao(PerfilNutricional pn) {
        FichaTecnica ft = new FichaTecnica();
        ft.setPerfilNutricional(pn);

        FichasPorRefeicao fp = new FichasPorRefeicao();
        fp.setFichaTecnica(ft);
        return fp;
    }
}
