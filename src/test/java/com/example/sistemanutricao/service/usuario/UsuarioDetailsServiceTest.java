package com.example.sistemanutricao.service.usuario;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.example.sistemanutricao.model.enuns.Cargo;
import com.example.sistemanutricao.model.Usuario;
import com.example.sistemanutricao.repository.UsuarioRepository;
import com.example.sistemanutricao.exception.UsuarioSemCargoException;

@ExtendWith(MockitoExtension.class)
class UsuarioDetailsServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioDetailsService usuarioDetailsService;

    @Test
    void shouldLoadUserByEmail() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("alice");
        usuario.setEmail("alice@example.com");
        usuario.setSenha("hash");
        usuario.setCargo(Cargo.ADMIN);
        usuario.setAtivo(true);

        when(usuarioRepository.findByEmailIgnoreCase("alice@example.com")).thenReturn(Optional.of(usuario));

        UserDetails userDetails = usuarioDetailsService.loadUserByUsername("alice@example.com");

        assertThat(userDetails.getUsername()).isEqualTo("alice");
        assertThat(userDetails.getAuthorities()).hasSize(1);
    }

    @Test
    void shouldFailWhenEmailDoesNotExist() {
        when(usuarioRepository.findByEmailIgnoreCase("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioDetailsService.loadUserByUsername("missing@example.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("E-mail não encontrado");
    }

    @Test
    void shouldFailWhenUserHasNoCargo() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("alice");
        usuario.setEmail("alice@example.com");
        usuario.setSenha("hash");
        usuario.setAtivo(true);

        when(usuarioRepository.findByEmailIgnoreCase("alice@example.com")).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> usuarioDetailsService.loadUserByUsername("alice@example.com"))
                .isInstanceOf(UsuarioSemCargoException.class)
                .hasMessage("Usuário sem cargo definido");
    }
}