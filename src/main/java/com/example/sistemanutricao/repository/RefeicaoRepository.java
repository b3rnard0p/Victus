package com.example.sistemanutricao.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.example.sistemanutricao.model.Refeicao;

public interface RefeicaoRepository extends JpaRepository<Refeicao, Long>, JpaSpecificationExecutor<Refeicao> {

    @EntityGraph(attributePaths = {"nutricionista"})
    @Override
    Page<Refeicao> findAll(org.springframework.data.jpa.domain.Specification<Refeicao> spec, Pageable pageable);

    boolean existsByNutricionistaIdAndNomeIgnoreCase(Long nutricionistaId, String nome);

    boolean existsByNutricionistaIdAndNomeIgnoreCaseAndIdNot(Long nutricionistaId, String nome, Long id);
    boolean existsByNutricionistaId(Long nutricionistaId);

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT r FROM Refeicao r JOIN r.fichasPorRefeicao fp WHERE fp.fichaTecnica.id = :fichaId")
    java.util.List<Refeicao> buscarPorFichaTecnicaId(@org.springframework.data.repository.query.Param("fichaId") Long fichaId);
}