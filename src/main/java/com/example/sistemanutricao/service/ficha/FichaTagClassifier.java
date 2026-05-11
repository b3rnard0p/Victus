package com.example.sistemanutricao.service.ficha;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.example.sistemanutricao.model.FichaTecnica;

@Component
public class FichaTagClassifier {

    public BigDecimal obterValorCampo(FichaTecnica ficha, String campo) {
        if (ficha == null || campo == null) return null;
        return switch (campo.toLowerCase()) {
            case "custopercapita" -> ficha.getCustoPerCapita();
            case "custototal"     -> ficha.getCustoTotal();
            case "rendimento"     -> ficha.getPreparacao() != null ? ficha.getPreparacao().getRendimento() : null;
            case "vtc"            -> ficha.getPerfilNutricional() != null ? ficha.getPerfilNutricional().getVtc() : null;
            case "gramasptn"      -> ficha.getPerfilNutricional() != null ? ficha.getPerfilNutricional().getGramasPTN() : null;
            case "gramascho"      -> ficha.getPerfilNutricional() != null ? ficha.getPerfilNutricional().getGramasCHO() : null;
            case "gramaslip"      -> ficha.getPerfilNutricional() != null ? ficha.getPerfilNutricional().getGramasLIP() : null;
            case "gramassodio"    -> ficha.getPerfilNutricional() != null ? ficha.getPerfilNutricional().getGramasSodio() : null;
            case "gramassaturada" -> ficha.getPerfilNutricional() != null ? ficha.getPerfilNutricional().getGramasSaturada() : null;
            default               -> null;
        };
    }

    public String determinarTag(FichaTecnica ficha, String campo) {
        if (ficha == null || campo == null) return "Baixa";

        BigDecimal valor = obterValorCampo(ficha, campo);
        if (valor == null) return "Baixa";

        return switch (campo.toLowerCase()) {
            case "custopercapita" -> classificar(valor, "5.00", "2.50");
            case "custototal"     -> classificar(valor, "100.00", "50.00");
            case "rendimento"     -> classificar(valor, "50", "25");
            case "vtc"            -> classificar(valor, "500", "300");
            case "gramasptn"      -> classificar(valor, "20", "10");
            case "gramascho"      -> classificar(valor, "60", "30");
            case "gramaslip"      -> classificar(valor, "20", "10");
            case "gramassodio"    -> classificar(valor, "1000", "500");
            case "gramassaturada" -> classificar(valor, "10", "5");
            default               -> "Baixa";
        };
    }

    private String classificar(BigDecimal valor, String limiteAlta, String limiteMedia) {
        if (valor.compareTo(new BigDecimal(limiteAlta)) >= 0)  return "Alta";
        if (valor.compareTo(new BigDecimal(limiteMedia)) >= 0) return "Media";
        return "Baixa";
    }
}
