package com.example.sistemanutricao.repository;

import com.example.sistemanutricao.model.Estabelecimento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EstabelecimentoRepository extends JpaRepository<Estabelecimento, Long> {
    boolean existsByNome(String nome);
    boolean existsByNomeAndIdNot(String nome, Long id);
}
