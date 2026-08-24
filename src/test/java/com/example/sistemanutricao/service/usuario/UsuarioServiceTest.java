package com.example.sistemanutricao.service.usuario;

import com.example.sistemanutricao.exception.UsuarioNotFoundException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

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
import com.example.sistemanutricao.exception.DuplicateNomeException;
import com.example.sistemanutricao.model.enuns.Cargo;
import com.example.sistemanutricao.model.Estabelecimento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import java.util.List;
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

    @Mock
    private com.example.sistemanutricao.repository.FichaTecnicaRepository fichaTecnicaRepository;

    @Mock
    private com.example.sistemanutricao.repository.RefeicaoRepository refeicaoRepository;

    @Mock
    private com.example.sistemanutricao.repository.IngredienteRepository ingredienteRepository;

    private UsuarioMapper usuarioMapper = Mappers.getMapper(UsuarioMapper.class);

    private UsuarioService usuarioService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        usuarioService = new UsuarioService(
                usuarioRepository,
                estabelecimentoRepository,
                passwordEncoder,
                imageStorage,
                usuarioMapper,
                fichaTecnicaRepository,
                refeicaoRepository,
                ingredienteRepository
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

    @Test
    void create_Success() {
        UsuarioDTO dto = new UsuarioDTO("user", "user@test.com", "senha", "senha", "senha", null, null);
        when(usuarioRepository.findByEmail("user@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("senha")).thenReturn("hash");

        Usuario saved = new Usuario();
        saved.setId(1L);
        when(usuarioRepository.save(any())).thenReturn(saved);

        Usuario result = usuarioService.create(dto);
        assertThat(result).isNotNull();
        verify(usuarioRepository).save(any());
    }

    @Test
    void create_DuplicateEmail() {
        UsuarioDTO dto = new UsuarioDTO("user", "user@test.com", "senha", "senha", "senha", null, null);
        when(usuarioRepository.findByEmail("user@test.com")).thenReturn(Optional.of(new Usuario()));

        assertThatThrownBy(() -> usuarioService.create(dto))
            .isInstanceOf(DuplicateNomeException.class);
    }

    @Test
    void toggleAtivo_Success() {
        Usuario usuario = new Usuario();
        usuario.setAtivo(false);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        usuarioService.toggleAtivo(1L);

        assertThat(usuario.isAtivo()).isTrue();
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void updateCargo_Success() {
        Usuario usuario = new Usuario();
        usuario.setCargo(Cargo.PRODUCAO);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        usuarioService.updateCargo(1L, Cargo.ADMIN);

        assertThat(usuario.getCargo()).isEqualTo(Cargo.ADMIN);
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void updateCargo_RemoverNutricionistaComFichas_LancaExcecao() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setCargo(Cargo.NUTRICIONISTA);
        
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(fichaTecnicaRepository.existsByNutricionistaId(1L)).thenReturn(true);
        
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
            usuarioService.updateCargo(1L, Cargo.PRODUCAO);
        });
    }

    @Test
    void updateCargo_RemoverNutricionistaComRefeicoes_LancaExcecao() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setCargo(Cargo.NUTRICIONISTA);
        
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(fichaTecnicaRepository.existsByNutricionistaId(1L)).thenReturn(false);
        when(refeicaoRepository.existsByNutricionistaId(1L)).thenReturn(true);
        
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
            usuarioService.updateCargo(1L, Cargo.ADMIN);
        });
    }

    @Test
    void updateCargo_RemoverNutricionistaComIngredientes_LancaExcecao() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setCargo(Cargo.NUTRICIONISTA);
        
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(fichaTecnicaRepository.existsByNutricionistaId(1L)).thenReturn(false);
        when(refeicaoRepository.existsByNutricionistaId(1L)).thenReturn(false);
        when(ingredienteRepository.existsByUsuario_Id(1L)).thenReturn(true);
        
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
            usuarioService.updateCargo(1L, Cargo.PRODUCAO);
        });
    }

    @Test
    void atualizarEstabelecimento_Success() {
        Usuario usuario = new Usuario();
        Estabelecimento est = new Estabelecimento();
        est.setId(10L);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(estabelecimentoRepository.findById(10L)).thenReturn(Optional.of(est));

        usuarioService.atualizarEstabelecimento(1L, 10L);

        assertThat(usuario.getEstabelecimento()).isEqualTo(est);
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void atualizarEstabelecimento_NullId() {
        Usuario usuario = new Usuario();
        usuario.setEstabelecimento(new Estabelecimento());
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        usuarioService.atualizarEstabelecimento(1L, null);

        assertThat(usuario.getEstabelecimento()).isNull();
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void inicializarAdminPadrao_Creation() {
        when(usuarioRepository.findByCargo(Cargo.ADMIN)).thenReturn(List.of());
        when(usuarioRepository.findByEmailIgnoreCase(any())).thenReturn(Optional.empty());

        usuarioService.inicializarAdminPadrao();

        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void listAll_Success() {
        when(usuarioRepository.findAdministradoresComuns(any(), any(), any())).thenReturn(new PageImpl<>(List.of(new Usuario())));
        List<GetUsuarioDTO> result = usuarioService.listAll();
        assertThat(result).hasSize(1);
    }
}