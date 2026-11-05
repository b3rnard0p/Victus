package com.example.sistemanutricao.config;

import java.math.BigDecimal;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Component
public class StringToBigDecimalConverter implements Converter<String, BigDecimal> {

    @Override
    @Nullable
    public BigDecimal convert(String source) {
        if (source == null) {
            return null;
        }
        String trimmed = source.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        if (trimmed.contains(",") && trimmed.contains(".")) {
            if (trimmed.indexOf(",") < trimmed.indexOf(".")) {
                trimmed = trimmed.replace(",", "");
            } else {
                trimmed = trimmed.replace(".", "").replace(",", ".");
            }
        } else if (trimmed.contains(",")) {
            trimmed = trimmed.replace(",", ".");
        }

        try {
            return new BigDecimal(trimmed);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Valor numérico inválido: " + source, e);
        }
    }
}
