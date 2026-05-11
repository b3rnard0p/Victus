package com.example.sistemanutricao.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.example.sistemanutricao.model.FichaTecnica;
import com.example.sistemanutricao.model.enuns.Status;
import com.example.sistemanutricao.model.enuns.StatusCriacao;

public interface FichaTecnicaRepository extends JpaRepository<FichaTecnica, Long>, JpaSpecificationExecutor<FichaTecnica> {

    @EntityGraph(value = "Ficha.completa")
    @Override
    Page<FichaTecnica> findAll(org.springframework.data.jpa.domain.Specification<FichaTecnica> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"preparacao", "perfilNutricional"})
    Page<FichaTecnica> findByStatusAndStatusCriacao(Status status, StatusCriacao statusCriacao, Pageable pageable);

    boolean existsByNutricionistaIdAndPreparacaoNomeIgnoreCase(Long nutricionistaId, String nome);

    boolean existsByNutricionistaIdAndPreparacaoNomeIgnoreCaseAndIdNot(Long nutricionistaId, String nome, Long id);

    boolean existsByNutricionistaIdAndPreparacaoNumero(Long nutricionistaId, Integer numero);

    boolean existsByNutricionistaIdAndPreparacaoNumeroAndIdNot(Long nutricionistaId, Integer numero, Long id);
}