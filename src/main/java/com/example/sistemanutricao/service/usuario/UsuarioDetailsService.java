package com.example.sistemanutricao.service.usuario;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.sistemanutricao.model.Usuario;
import com.example.sistemanutricao.repository.UsuarioRepository;
import com.example.sistemanutricao.security.UsuarioSecurity;
import com.example.sistemanutricao.exception.UsuarioSemCargoException;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository repo;

    public UsuarioDetailsService(UsuarioRepository repo) {
        this.repo = repo;
    }

    @Override
    @Cacheable(value = "usuarios", key = "#email", unless = "#result == null")
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        
        Usuario user = repo.findByEmailIgnoreCase(email)
                .orElseThrow(() -> {
                    return new UsernameNotFoundException("E-mail não encontrado");
                });
        try {
            org.slf4j.LoggerFactory.getLogger(UsuarioDetailsService.class)
                .info("[DEBUG] Carregando UserDetails para {} - hash: {}", email, user.getSenha());
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(UsuarioDetailsService.class)
                .warn("[DEBUG] Não foi possível logar hash do usuário: {}", e.getMessage());
        }

        if (user.getCargo() == null) {
            throw new UsuarioSemCargoException("Usuário sem cargo definido");
        }

        return new UsuarioSecurity(user);
    }
}
