package com.example.sistemanutricao.repository.specification;

import com.example.sistemanutricao.model.Ingrediente;
import com.example.sistemanutricao.model.enuns.Status;
import org.springframework.data.jpa.domain.Specification;

public class IngredienteSpecification {

    public static Specification<Ingrediente> filter(
            Status status,
            Long usuarioId,
            String campo,
            Object valor
    ) {
        return (root, query, cb) -> {
            var predicates = cb.conjunction();

            if (status != null) {
                predicates = cb.and(predicates, cb.equal(root.get("status"), status));
            }

            if (usuarioId != null) {
                predicates = cb.and(predicates, cb.equal(root.get("usuario").get("id"), usuarioId));
            }

            if (campo != null && valor != null) {
                switch (campo.toLowerCase()) {
                    case "nome" -> predicates = cb.and(predicates, cb.like(cb.lower(root.get("nome")), "%" + valor.toString().toLowerCase() + "%"));
                    case "ptn" -> predicates = cb.and(predicates, cb.equal(root.get("ptn"), valor));
                    case "cho" -> predicates = cb.and(predicates, cb.equal(root.get("cho"), valor));
                    case "lip" -> predicates = cb.and(predicates, cb.equal(root.get("lip"), valor));
                    case "sodio" -> predicates = cb.and(predicates, cb.equal(root.get("sodio"), valor));
                    case "gorduras", "gordurasaturada" -> predicates = cb.and(predicates, cb.equal(root.get("gorduraSaturada"), valor));
                }
            }

            return predicates;
        };
    }
}
