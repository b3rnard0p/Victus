package com.example.sistemanutricao.config;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StringToBigDecimalConverterTest {

    private StringToBigDecimalConverter converter;

    @BeforeEach
    void setUp() {
        converter = new StringToBigDecimalConverter();
    }

    @Test
    void deveRetornarNullParaStringNulaOuVazia() {
        assertNull(converter.convert(null));
        assertNull(converter.convert(""));
        assertNull(converter.convert("   "));
    }

    @Test
    void deveConverterNumeroComVirgulaDecimal() {
        BigDecimal resultado = converter.convert("222,9");
        assertNotNull(resultado);
        assertEquals(new BigDecimal("222.9"), resultado);
    }

    @Test
    void deveConverterNumeroComPontoDecimal() {
        BigDecimal resultado = converter.convert("222.9");
        assertNotNull(resultado);
        assertEquals(new BigDecimal("222.9"), resultado);
    }

    @Test
    void deveConverterNumeroInteiro() {
        BigDecimal resultado = converter.convert("222");
        assertNotNull(resultado);
        assertEquals(new BigDecimal("222"), resultado);
    }

    @Test
    void deveConverterFormatoBrasileiroComMilharEVirgula() {
        BigDecimal resultado = converter.convert("1.234,56");
        assertNotNull(resultado);
        assertEquals(new BigDecimal("1234.56"), resultado);
    }

    @Test
    void deveConverterFormatoAmericanoComMilharEPonto() {
        BigDecimal resultado = converter.convert("1,234.56");
        assertNotNull(resultado);
        assertEquals(new BigDecimal("1234.56"), resultado);
    }

    @Test
    void deveTratarEspacosEmBranco() {
        BigDecimal resultado = converter.convert("  350,75  ");
        assertNotNull(resultado);
        assertEquals(new BigDecimal("350.75"), resultado);
    }

    @Test
    void deveLancarExcecaoParaTextoInvalido() {
        assertThrows(IllegalArgumentException.class, () -> converter.convert("abc"));
    }
}

