package com.example.sistemanutricao.service.ficha;

import com.example.sistemanutricao.model.Ingrediente;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;

class IngredienteTagClassifierTest {

    private final IngredienteTagClassifier classifier = new IngredienteTagClassifier();

    @Test
    void determinarTag_Ptn() {
        Ingrediente ing = new Ingrediente();
        ing.setPtn(new BigDecimal("12"));
        assertThat(classifier.determinarTag(ing, "ptn")).isEqualTo("Alta");

        ing.setPtn(new BigDecimal("6"));
        assertThat(classifier.determinarTag(ing, "ptn")).isEqualTo("Media");

        ing.setPtn(new BigDecimal("2"));
        assertThat(classifier.determinarTag(ing, "ptn")).isEqualTo("Baixa");
    }

    @Test
    void determinarTag_Cho() {
        Ingrediente ing = new Ingrediente();
        ing.setCho(new BigDecimal("35"));
        assertThat(classifier.determinarTag(ing, "cho")).isEqualTo("Alta");

        ing.setCho(new BigDecimal("20"));
        assertThat(classifier.determinarTag(ing, "cho")).isEqualTo("Media");

        ing.setCho(new BigDecimal("10"));
        assertThat(classifier.determinarTag(ing, "cho")).isEqualTo("Baixa");
    }

    @Test
    void determinarTag_Lip() {
        Ingrediente ing = new Ingrediente();
        ing.setLip(new BigDecimal("15"));
        assertThat(classifier.determinarTag(ing, "lip")).isEqualTo("Alta");

        ing.setLip(new BigDecimal("7"));
        assertThat(classifier.determinarTag(ing, "lip")).isEqualTo("Media");

        ing.setLip(new BigDecimal("3"));
        assertThat(classifier.determinarTag(ing, "lip")).isEqualTo("Baixa");
    }

    @Test
    void determinarTag_Sodio() {
        Ingrediente ing = new Ingrediente();
        ing.setSodio(new BigDecimal("600"));
        assertThat(classifier.determinarTag(ing, "sodio")).isEqualTo("Alta");

        ing.setSodio(new BigDecimal("300"));
        assertThat(classifier.determinarTag(ing, "sodio")).isEqualTo("Media");

        ing.setSodio(new BigDecimal("100"));
        assertThat(classifier.determinarTag(ing, "sodio")).isEqualTo("Baixa");
    }

    @Test
    void determinarTag_Gorduras() {
        Ingrediente ing = new Ingrediente();
        ing.setGorduraSaturada(new BigDecimal("6"));
        assertThat(classifier.determinarTag(ing, "gorduras")).isEqualTo("Alta");

        ing.setGorduraSaturada(new BigDecimal("3"));
        assertThat(classifier.determinarTag(ing, "gorduras")).isEqualTo("Media");

        ing.setGorduraSaturada(new BigDecimal("1"));
        assertThat(classifier.determinarTag(ing, "gorduras")).isEqualTo("Baixa");
    }

    @Test
    void determinarTag_Unknown() {
        Ingrediente ing = new Ingrediente();
        assertThat(classifier.determinarTag(ing, "unknown")).isEqualTo("Baixa");
        assertThat(classifier.determinarTag(ing, "ptn")).isEqualTo("Baixa"); // null value
    }
}
