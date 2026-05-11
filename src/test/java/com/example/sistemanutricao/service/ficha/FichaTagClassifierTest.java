package com.example.sistemanutricao.service.ficha;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.example.sistemanutricao.model.FichaTecnica;
import com.example.sistemanutricao.model.PerfilNutricional;
import com.example.sistemanutricao.model.Preparacao;

class FichaTagClassifierTest {

    private final FichaTagClassifier classifier = new FichaTagClassifier();

    private FichaTecnica fichaComVtc(BigDecimal vtc) {
        PerfilNutricional perfil = new PerfilNutricional();
        perfil.setVtc(vtc);
        FichaTecnica ficha = new FichaTecnica();
        ficha.setPerfilNutricional(perfil);
        return ficha;
    }

    private FichaTecnica fichaComCusto(BigDecimal perCapita, BigDecimal total) {
        FichaTecnica ficha = new FichaTecnica();
        ficha.setCustoPerCapita(perCapita);
        ficha.setCustoTotal(total);
        return ficha;
    }

    private FichaTecnica fichaComRendimento(BigDecimal rendimento) {
        Preparacao prep = new Preparacao();
        prep.setRendimento(rendimento);
        FichaTecnica ficha = new FichaTecnica();
        ficha.setPreparacao(prep);
        return ficha;
    }

    @ParameterizedTest
    @CsvSource({
        "600, Alta",
        "400, Media",
        "100, Baixa"
    })
    void shouldClassifyVtcCorrectly(BigDecimal vtc, String esperada) {
        FichaTecnica ficha = fichaComVtc(vtc);
        assertThat(classifier.determinarTag(ficha, "vtc")).isEqualTo(esperada);
    }

    @ParameterizedTest
    @CsvSource({
        "6.00, Alta",
        "3.00, Media",
        "1.00, Baixa"
    })
    void shouldClassifyCustoPerCapitaCorrectly(BigDecimal custo, String esperada) {
        FichaTecnica ficha = fichaComCusto(custo, BigDecimal.ZERO);
        assertThat(classifier.determinarTag(ficha, "custopercapita")).isEqualTo(esperada);
    }

    @ParameterizedTest
    @CsvSource({
        "200, Alta",
        "75,  Media",
        "10,  Baixa"
    })
    void shouldClassifyCustoTotalCorrectly(BigDecimal custo, String esperada) {
        FichaTecnica ficha = fichaComCusto(BigDecimal.ZERO, custo);
        assertThat(classifier.determinarTag(ficha, "custototal")).isEqualTo(esperada);
    }

    @ParameterizedTest
    @CsvSource({
        "60, Alta",
        "30, Media",
        "5,  Baixa"
    })
    void shouldClassifyRendimentoCorrectly(BigDecimal rend, String esperada) {
        FichaTecnica ficha = fichaComRendimento(rend);
        assertThat(classifier.determinarTag(ficha, "rendimento")).isEqualTo(esperada);
    }

    @Test
    void shouldReturnBaixaForNullFicha() {
        assertThat(classifier.determinarTag(null, "vtc")).isEqualTo("Baixa");
    }

    @Test
    void shouldReturnBaixaForUnknownCampo() {
        FichaTecnica ficha = fichaComVtc(new BigDecimal("500"));
        assertThat(classifier.determinarTag(ficha, "campoDesconhecido")).isEqualTo("Baixa");
    }

    @Test
    void shouldReturnBaixaWhenPerfilNutricionalIsNull() {
        FichaTecnica ficha = new FichaTecnica();
        assertThat(classifier.determinarTag(ficha, "vtc")).isEqualTo("Baixa");
    }

    @Test
    void shouldExtractValorCampoCorrectly() {
        FichaTecnica ficha = fichaComVtc(new BigDecimal("350"));
        assertThat(classifier.obterValorCampo(ficha, "vtc")).isEqualByComparingTo("350");
        assertThat(classifier.obterValorCampo(ficha, "custopercapita")).isNull();
        assertThat(classifier.obterValorCampo(null, "vtc")).isNull();
    }
}
