package com.example.sistemanutricao.service.usuario;

import com.example.sistemanutricao.exception.UsuarioNotFoundException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.sistemanutricao.mapper.UsuarioMapper;
import com.example.sistemanutricao.model.Usuario;
import com.example.sistemanutricao.record.UsuarioDTO.GetUsuarioDTO;
import com.example.sistemanutricao.record.UsuarioDTO.UsuarioDTO;
import com.example.sistemanutricao.repository.EstabelecimentoRepository;
import com.example.sistemanutricao.repository.UsuarioRepository;
import com.example.sistemanutricao.service.port.ImageStorage;
import org.mapstruct.factory.Mappers;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private EstabelecimentoRepository estabelecimentoRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ImageStorage imageStorage;

    private UsuarioMapper usuarioMapper = Mappers.getMapper(UsuarioMapper.class);

    private UsuarioService usuarioService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        usuarioService = new UsuarioService(
                usuarioRepository,
                estabelecimentoRepository,
                passwordEncoder,
                imageStorage,
                usuarioMapper
        );
    }

    @Test
    void shouldReturnUsuarioDtoWhenFoundById() {
        Usuario usuario = new Usuario();
        usuario.setId(10L);
        usuario.setUsername("Ana");
        usuario.setEmail("ana@exemplo.com");
        usuario.setAtivo(true);

        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario));

        GetUsuarioDTO dto = usuarioService.findById(10L);

        assertThat(dto.id()).isEqualTo(10L);
        assertThat(dto.username()).isEqualTo("Ana");
        assertThat(dto.email()).isEqualTo("ana@exemplo.com");
        assertThat(dto.ativo()).isTrue();
        verify(usuarioRepository).findById(10L);
    }

    @Test
    void shouldThrowUsuarioNotFoundExceptionWhenUsuarioDoesNotExist() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.findById(99L))
                .isInstanceOf(UsuarioNotFoundException.class)
                .hasMessage("Usuário não encontrado");
    }

    @Test
    void shouldUpdateUsuarioPasswordWhenNewPasswordIsProvided() {
        Usuario usuario = new Usuario();
        usuario.setId(7L);
        usuario.setUsername("Maria");
        usuario.setEmail("maria@exemplo.com");
        usuario.setSenha("senha-antiga");
        usuario.setAtivo(true);

        UsuarioDTO dto = new UsuarioDTO(
                "Maria Nova",
                "maria.nova@exemplo.com",
                null,
                "novaSenha123",
                "novaSenha123",
                null,
                null
        );


        when(usuarioRepository.findById(7L)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.encode("novaSenha123")).thenReturn("senha-criptografada");
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        GetUsuarioDTO atualizado = usuarioService.update(7L, dto);

        assertThat(usuario.getUsername()).isEqualTo("Maria Nova");
        assertThat(usuario.getEmail()).isEqualTo("maria.nova@exemplo.com");
        assertThat(usuario.getSenha()).isEqualTo("senha-criptografada");
        assertThat(atualizado.username()).isEqualTo("Maria Nova");
        assertThat(atualizado.email()).isEqualTo("maria.nova@exemplo.com");
        verify(passwordEncoder).encode("novaSenha123");
        verify(usuarioRepository).save(usuario);
    }
}