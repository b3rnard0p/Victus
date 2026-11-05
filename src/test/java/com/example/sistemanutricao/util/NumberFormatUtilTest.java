package com.example.sistemanutricao.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NumberFormatUtilTest {

    private final NumberFormatUtil util = new NumberFormatUtil();

    @Test
    @DisplayName("Deve formatar valores nulos e zero como '0'")
    void deveFormatarNuloEZero() {
        assertEquals("0", NumberFormatUtil.formatar(null));
        assertEquals("0", NumberFormatUtil.formatar(BigDecimal.ZERO));
        assertEquals("0", NumberFormatUtil.formatar(new BigDecimal("0.00000")));
        assertEquals("0", util.formatarQuantidade(null));
    }

    @Test
    @DisplayName("Deve remover zeros decimais desnecessários")
    void deveRemoverZerosDecimaisDesnecessarios() {
        assertEquals("111", NumberFormatUtil.formatar(new BigDecimal("111.00000")));
        assertEquals("1", NumberFormatUtil.formatar(new BigDecimal("1.00")));
        assertEquals("500", NumberFormatUtil.formatar(new BigDecimal("500.000")));
    }

    @Test
    @DisplayName("Deve manter até 3 casas decimais quando houver dígitos significativos")
    void deveManterAte3CasasDecimais() {
        assertEquals("111,5", NumberFormatUtil.formatar(new BigDecimal("111.50000")));
        assertEquals("111,25", NumberFormatUtil.formatar(new BigDecimal("111.25000")));
        assertEquals("111,125", NumberFormatUtil.formatar(new BigDecimal("111.12500")));
        assertEquals("1,25", NumberFormatUtil.formatar(new BigDecimal("1.25")));
    }

    @Test
    @DisplayName("Deve arredondar casas além da 3ª decimal com HALF_UP")
    void deveArredondarApos3Casas() {
        assertEquals("111,125", NumberFormatUtil.formatar(new BigDecimal("111.12544")));
        assertEquals("111,126", NumberFormatUtil.formatar(new BigDecimal("111.12560")));
    }

    @Test
    @DisplayName("Deve limpar casas decimais extras em BigDecimal para edição")
    void deveLimparDecimais() {
        assertEquals("2", NumberFormatUtil.limparDecimais(new BigDecimal("2.00000")).toString());
        assertEquals("222", NumberFormatUtil.limparDecimais(new BigDecimal("222.00000")).toString());
        assertEquals("1", NumberFormatUtil.limparDecimais(new BigDecimal("1.00")).toString());
        assertEquals("2.5", NumberFormatUtil.limparDecimais(new BigDecimal("2.50000")).toString());
        assertEquals("2.125", NumberFormatUtil.limparDecimais(new BigDecimal("2.12500")).toString());
        assertEquals("1000", NumberFormatUtil.limparDecimais(new BigDecimal("1000.00000")).toString());
        assertEquals(BigDecimal.ZERO, NumberFormatUtil.limparDecimais(new BigDecimal("0.00000")));
    }
}

