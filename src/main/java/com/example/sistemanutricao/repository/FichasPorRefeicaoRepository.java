package com.example.sistemanutricao.repository;

import com.example.sistemanutricao.model.FichasPorRefeicao;
import com.example.sistemanutricao.model.FichasPorRefeicaoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FichasPorRefeicaoRepository extends JpaRepository<FichasPorRefeicao, FichasPorRefeicaoId> {
    void deleteByRefeicaoId(Long refeicaoId);
}