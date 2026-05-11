package com.example.sistemanutricao.repository.specification;

import com.example.sistemanutricao.model.FichaTecnica;
import com.example.sistemanutricao.model.enuns.Status;
import com.example.sistemanutricao.model.enuns.StatusCriacao;
import com.example.sistemanutricao.model.enuns.Categoria;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Path;
import java.math.BigDecimal;

public class FichaTecnicaSpecification {

    public static Specification<FichaTecnica> filter(
            Status status,
            StatusCriacao statusCriacao,
            Long nutricionistaId,
            Long estabelecimentoId,
            String campo,
            Object valor
    ) {
        return (root, query, cb) -> {
            var predicates = cb.conjunction();

            if (status != null) {
                predicates = cb.and(predicates, cb.equal(root.get("status"), status));
            }

            if (statusCriacao != null) {
                predicates = cb.and(predicates, cb.equal(root.get("statusCriacao"), statusCriacao));
            }

            if (nutricionistaId != null) {
                predicates = cb.and(predicates, cb.equal(root.get("nutricionista").get("id"), nutricionistaId));
            }

            if (estabelecimentoId != null) {
                predicates = cb.and(predicates, cb.equal(root.get("nutricionista").get("estabelecimento").get("id"), estabelecimentoId));
            }

            if (campo != null && valor != null) {
                switch (campo) {
                    case "por-nome" -> predicates = cb.and(predicates, cb.like(cb.lower(root.get("preparacao").get("nome")), "%" + valor.toString().toLowerCase() + "%"));
                    case "por-numero", "preparacaoNumero" -> predicates = cb.and(predicates, cb.equal(root.get("preparacao").get("numero"), valor));
                    case "por-categoria" -> {
                        if (valor instanceof Categoria cat) {
                            predicates = cb.and(predicates, cb.equal(root.get("preparacao").get("categoria"), cat));
                        }
                    }
                    case "custoPerCapita" -> predicates = cb.and(predicates, cb.equal(root.get("custoPerCapita"), valor));
                    case "custoTotal" -> predicates = cb.and(predicates, cb.equal(root.get("custoTotal"), valor));
                    case "por-rendimento", "preparacaoRendimento" -> predicates = cb.and(predicates, cb.equal(root.get("preparacao").get("rendimento"), valor));
                    case "vtc" -> predicates = cb.and(predicates, cb.equal(root.get("perfilNutricional").get("vtc"), valor));
                    case "gramasPTN" -> predicates = cb.and(predicates, cb.equal(root.get("perfilNutricional").get("gramasPTN"), valor));
                    case "gramasCHO" -> predicates = cb.and(predicates, cb.equal(root.get("perfilNutricional").get("gramasCHO"), valor));
                    case "gramasLIP" -> predicates = cb.and(predicates, cb.equal(root.get("perfilNutricional").get("gramasLIP"), valor));
                    case "gramasSodio" -> predicates = cb.and(predicates, cb.equal(root.get("perfilNutricional").get("gramasSodio"), valor));
                    case "gramasSaturada" -> predicates = cb.and(predicates, cb.equal(root.get("perfilNutricional").get("gramasSaturada"), valor));
                }
            }

            return predicates;
        };
    }

    public static Specification<FichaTecnica> byTag(String campo, String tag) {
        return (root, query, cb) -> {
            if (campo == null || tag == null) return cb.conjunction();

            Path<BigDecimal> path = switch (campo.toLowerCase()) {
                case "custopercapita" -> root.get("custoPerCapita");
                case "custototal"     -> root.get("custoTotal");
                case "rendimento"     -> root.get("preparacao").get("rendimento");
                case "vtc"            -> root.get("perfilNutricional").get("vtc");
                case "gramasptn"      -> root.get("perfilNutricional").get("gramasPTN");
                case "gramascho"      -> root.get("perfilNutricional").get("gramasCHO");
                case "gramaslip"      -> root.get("perfilNutricional").get("gramasLIP");
                case "gramassodio"    -> root.get("perfilNutricional").get("gramasSodio");
                case "gramassaturada" -> root.get("perfilNutricional").get("gramasSaturada");
                default               -> null;
            };

            if (path == null) return cb.conjunction();

            BigDecimal alta = getLimite(campo, "Alta");
            BigDecimal media = getLimite(campo, "Media");

            if (tag.equalsIgnoreCase("Alta")) {
                return cb.greaterThanOrEqualTo(path, alta);
            } else if (tag.equalsIgnoreCase("Media")) {
                return cb.and(cb.greaterThanOrEqualTo(path, media), cb.lessThan(path, alta));
            } else {
                return cb.lessThan(path, media);
            }
        };
    }

    private static BigDecimal getLimite(String campo, String tipo) {
        boolean isAlta = "Alta".equalsIgnoreCase(tipo);
        return switch (campo.toLowerCase()) {
            case "custopercapita" -> new BigDecimal(isAlta ? "5.00" : "2.50");
            case "custototal"     -> new BigDecimal(isAlta ? "100.00" : "50.00");
            case "rendimento"     -> new BigDecimal(isAlta ? "50" : "25");
            case "vtc"            -> new BigDecimal(isAlta ? "500" : "300");
            case "gramasptn"      -> new BigDecimal(isAlta ? "20" : "10");
            case "gramascho"      -> new BigDecimal(isAlta ? "60" : "30");
            case "gramaslip"      -> new BigDecimal(isAlta ? "20" : "10");
            case "gramassodio"    -> new BigDecimal(isAlta ? "1000" : "500");
            case "gramassaturada" -> new BigDecimal(isAlta ? "10" : "5");
            default               -> BigDecimal.ZERO;
        };
    }
}
