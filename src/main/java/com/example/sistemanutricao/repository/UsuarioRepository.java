package com.example.sistemanutricao.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.sistemanutricao.model.enuns.Cargo;
import com.example.sistemanutricao.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String username);
    Optional<Usuario> findByUsernameIgnoreCase(String username);
    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findByEmailIgnoreCase(String email);

    List<Usuario> findByCargo(Cargo cargo);

    @Query("SELECT u FROM Usuario u WHERE u.id <> :excluirId AND LOWER(COALESCE(u.username, '')) <> LOWER(:excluirUsername)")
    Page<Usuario> findAdministradoresComuns(Long excluirId, String excluirUsername, Pageable pageable);
}
