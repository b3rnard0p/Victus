package com.example.sistemanutricao.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.sistemanutricao.model.Ingrediente;
import com.example.sistemanutricao.model.enuns.Status;

public interface IngredienteRepository extends JpaRepository<Ingrediente, Long>, JpaSpecificationExecutor<Ingrediente> {

    @EntityGraph(value = "Ingrediente.comUsuario")
    @Override
    Page<Ingrediente> findAll(org.springframework.data.jpa.domain.Specification<Ingrediente> spec, Pageable pageable);

    @EntityGraph(value = "Ingrediente.comUsuario")
    @Query("SELECT i FROM Ingrediente i WHERE i.status = :status AND i.usuario.id IN :usuariosIds")
    List<Ingrediente> findByStatusAndUsuarioIdIn(@Param("status") Status status,
                                                 @Param("usuariosIds") List<Long> usuariosIds);

    boolean existsByNomeIgnoreCaseAndUsuario_IdAndStatus(String nome, Long usuarioId, Status status);

    boolean existsByNomeIgnoreCaseAndUsuario_IdAndStatusAndIdNot(String nome, Long usuarioId, Status status, Long id);
}