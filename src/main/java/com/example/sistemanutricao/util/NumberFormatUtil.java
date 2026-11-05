package com.example.sistemanutricao.util;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

@Component("formatador")
public class NumberFormatUtil {

    private static final Locale LOCALE_PT_BR = Locale.forLanguageTag("pt-BR");

    public String formatarQuantidade(BigDecimal valor) {
        return formatar(valor);
    }

    public static BigDecimal limparDecimais(BigDecimal valor) {
        if (valor == null) {
            return null;
        }
        if (valor.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal scaled = valor.setScale(3, RoundingMode.HALF_UP).stripTrailingZeros();
        if (scaled.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        if (scaled.scale() < 0) {
            return scaled.setScale(0);
        }
        return scaled;
    }

    public static String formatar(BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) == 0) {
            return "0";
        }
        BigDecimal scaled = valor.setScale(3, RoundingMode.HALF_UP).stripTrailingZeros();
        if (scaled.compareTo(BigDecimal.ZERO) == 0) {
            return "0";
        }
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(LOCALE_PT_BR);
        DecimalFormat df = new DecimalFormat("0.###", symbols);
        return df.format(scaled);
    }
}

