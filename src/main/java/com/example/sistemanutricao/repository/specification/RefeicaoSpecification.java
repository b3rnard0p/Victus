package com.example.sistemanutricao.repository.specification;

import com.example.sistemanutricao.model.Refeicao;
import com.example.sistemanutricao.model.enuns.Status;
import org.springframework.data.jpa.domain.Specification;

public class RefeicaoSpecification {

    public static Specification<Refeicao> filter(
            Status status,
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

            if (nutricionistaId != null) {
                predicates = cb.and(predicates, cb.equal(root.get("nutricionista").get("id"), nutricionistaId));
            }

            if (estabelecimentoId != null) {
                predicates = cb.and(predicates, cb.equal(root.get("nutricionista").get("estabelecimento").get("id"), estabelecimentoId));
            }

            if (campo != null && valor != null) {
                if ("nome".equalsIgnoreCase(campo) || "por-nome".equalsIgnoreCase(campo)) {
                    predicates = cb.and(predicates, cb.like(cb.lower(root.get("nome")), "%" + valor.toString().toLowerCase() + "%"));
                }
            }

            return predicates;
        };
    }
}
