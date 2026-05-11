package com.example.sistemanutricao.service.ficha;

import com.example.sistemanutricao.model.Ingrediente;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class IngredienteTagClassifier {

    public String determinarTag(Ingrediente ingrediente, String campo) {
        BigDecimal valor = obterValorCampo(ingrediente, campo);
        if (valor == null) return "Baixa";

        return switch (campo.toLowerCase()) {
            case "ptn" -> valor.compareTo(new BigDecimal("10")) >= 0 ? "Alta" :
                    valor.compareTo(new BigDecimal("5")) >= 0 ? "Media" : "Baixa";
            case "cho" -> valor.compareTo(new BigDecimal("30")) >= 0 ? "Alta" :
                    valor.compareTo(new BigDecimal("15")) >= 0 ? "Media" : "Baixa";
            case "lip" -> valor.compareTo(new BigDecimal("10")) >= 0 ? "Alta" :
                    valor.compareTo(new BigDecimal("5")) >= 0 ? "Media" : "Baixa";
            case "sodio" -> valor.compareTo(new BigDecimal("500")) >= 0 ? "Alta" :
                    valor.compareTo(new BigDecimal("200")) >= 0 ? "Media" : "Baixa";
            case "gorduras" -> valor.compareTo(new BigDecimal("5")) >= 0 ? "Alta" :
                    valor.compareTo(new BigDecimal("2")) >= 0 ? "Media" : "Baixa";
            default -> "Baixa";
        };
    }

    private BigDecimal obterValorCampo(Ingrediente ingrediente, String campo) {
        return switch (campo.toLowerCase()) {
            case "ptn" -> ingrediente.getPtn();
            case "cho" -> ingrediente.getCho();
            case "lip" -> ingrediente.getLip();
            case "sodio" -> ingrediente.getSodio();
            case "gorduras" -> ingrediente.getGorduraSaturada();
            default -> null;
        };
    }
}
