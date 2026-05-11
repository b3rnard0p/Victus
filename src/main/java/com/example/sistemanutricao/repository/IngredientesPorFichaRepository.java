package com.example.sistemanutricao.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.sistemanutricao.model.IngredientesPorFicha;

public interface IngredientesPorFichaRepository extends JpaRepository<IngredientesPorFicha, Long> {
    @Query("SELECT i FROM IngredientesPorFicha i WHERE i.fichaTecnica.id = :fichaId")
    List<IngredientesPorFicha> buscarPorFichaTecnicaId(@Param("fichaId") Long fichaId);

}