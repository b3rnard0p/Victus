package com.example.sistemanutricao.service;

import com.example.sistemanutricao.exception.UsuarioNotFoundException;
import com.example.sistemanutricao.mapper.UsuarioMapper;
import com.example.sistemanutricao.model.Usuario;
import com.example.sistemanutricao.record.UsuarioDTO.GetUsuarioDTO;
import com.example.sistemanutricao.record.UsuarioDTO.UsuarioDTO;
import com.example.sistemanutricao.repository.EstabelecimentoRepository;
import com.example.sistemanutricao.repository.UsuarioRepository;
import com.example.sistemanutricao.service.port.ImageStorage;
import com.example.sistemanutricao.service.usuario.UsuarioService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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

    @Mock
    private UsuarioMapper usuarioMapper;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void atualizarPerfilComImagem_NotFound() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        UsuarioDTO dto = new UsuarioDTO("test", "test@test.com", "123", "test", "test", "test", "test");
        assertThrows(UsuarioNotFoundException.class, () -> 
            usuarioService.atualizarPerfilComImagem(1L, dto, null)
        );
    }

    @Test
    void atualizarPerfilComImagem_Success() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        
        GetUsuarioDTO getDto = new GetUsuarioDTO(1L, "test", "test@test.com", null, null, null, false, null);
        when(usuarioMapper.toGetDTO(usuario)).thenReturn(getDto);

        UsuarioDTO dto = new UsuarioDTO("test", "test@test.com", "123", "test", "test", "test", "test");
        GetUsuarioDTO result = usuarioService.atualizarPerfilComImagem(1L, dto, null);

        assertNotNull(result);
        assertEquals(1L, result.id());
    }
}
