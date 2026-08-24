package com.example.sistemanutricao.repository;

import com.example.sistemanutricao.model.AtividadeLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AtividadeLogRepository extends JpaRepository<AtividadeLog, Long> {
    
    @Query("SELECT a FROM AtividadeLog a WHERE a.usuario.id = :usuarioId ORDER BY a.dataHora DESC")
    Page<AtividadeLog> findByUsuarioIdOrderByDataHoraDesc(@Param("usuarioId") Long usuarioId, Pageable pageable);
}
