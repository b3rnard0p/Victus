package com.example.sistemanutricao.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.example.sistemanutricao.exception.RefeicaoNotFoundException;
import com.example.sistemanutricao.exception.UsuarioNotFoundException;
import com.example.sistemanutricao.model.FichaTecnica;
import com.example.sistemanutricao.model.FichasPorRefeicao;
import com.example.sistemanutricao.model.FichasPorRefeicaoId;
import com.example.sistemanutricao.model.PerfilNutricional;
import com.example.sistemanutricao.model.Preparacao;
import com.example.sistemanutricao.model.Refeicao;
import com.example.sistemanutricao.model.enuns.Status;
import com.example.sistemanutricao.model.Usuario;
import com.example.sistemanutricao.record.RefeicaoDTO.RefeicaoDTO;
import com.example.sistemanutricao.record.RefeicaoDTO.RefeicaoResponseDTO;
import com.example.sistemanutricao.mapper.RefeicaoMapper;
import com.example.sistemanutricao.repository.FichaTecnicaRepository;
import com.example.sistemanutricao.repository.RefeicaoRepository;
import com.example.sistemanutricao.repository.UsuarioRepository;
import com.example.sistemanutricao.service.refeicao.RefeicaoService;
import org.mapstruct.factory.Mappers;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class RefeicaoServiceTest {

    @Mock
    private RefeicaoRepository refeicaoRepository;

    @Mock
    private FichaTecnicaRepository fichaTecnicaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    private RefeicaoMapper refeicaoMapper = Mappers.getMapper(RefeicaoMapper.class);

    private RefeicaoService refeicaoService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        refeicaoService = new RefeicaoService(
                refeicaoRepository,
                fichaTecnicaRepository,
                usuarioRepository,
                refeicaoMapper
        );
    }

    // =========================================================================
    // BUSCAR POR ID
    // =========================================================================

    @Test
    void shouldReturnRefeicaoWhenFoundById() {
        Refeicao refeicao = criarRefeicao(3L, "Almoço");

        when(refeicaoRepository.findById(3L)).thenReturn(Optional.of(refeicao));

        RefeicaoResponseDTO dto = refeicaoService.buscarPorId(3L);

        assertThat(dto.id()).isEqualTo(3L);
        assertThat(dto.nome()).isEqualTo("Almoço");
        assertThat(dto.status()).isEqualTo(Status.ATIVA);
    }

    @Test
    void shouldThrowRefeicaoNotFoundExceptionWhenMissing() {
        when(refeicaoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refeicaoService.buscarPorId(99L))
                .isInstanceOf(RefeicaoNotFoundException.class)
                .hasMessage("Refeição não encontrada");
    }

    // =========================================================================
    // CREATE
    // =========================================================================

    @Test
    void shouldCreateRefeicaoSuccessfully() {
        Usuario nutricionista = criarUsuario(1L);
        RefeicaoDTO dto = new RefeicaoDTO("Café da Manhã", "300", Status.ATIVA, List.of(10L));

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(nutricionista));
        when(refeicaoRepository.existsByNutricionistaIdAndNomeIgnoreCase(1L, "Café da Manhã")).thenReturn(false);
        when(refeicaoRepository.save(any(Refeicao.class))).thenAnswer(invocation -> {
            Refeicao r = invocation.getArgument(0);
            r.setId(100L);
            return r;
        });
        when(fichaTecnicaRepository.findAllById(List.of(10L))).thenReturn(List.of(criarFichaTecnica(10L)));

        RefeicaoResponseDTO result = refeicaoService.create(dto, 1L);

        assertThat(result.id()).isEqualTo(100L);
        assertThat(result.nome()).isEqualTo("Café da Manhã");
        verify(refeicaoRepository).save(any(Refeicao.class));
    }

    @Test
    void shouldThrowWhenCreatingWithDuplicateName() {
        RefeicaoDTO dto = new RefeicaoDTO("Almoço", "500", Status.ATIVA, List.of(10L));

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(criarUsuario(1L)));
        when(refeicaoRepository.existsByNutricionistaIdAndNomeIgnoreCase(1L, "Almoço")).thenReturn(true);

        assertThatThrownBy(() -> refeicaoService.create(dto, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Já existe uma refeição com este nome.");
    }

    @Test
    void shouldThrowWhenCreatingWithEmptyFichas() {
        RefeicaoDTO dto = new RefeicaoDTO("Almoço", "500", Status.ATIVA, List.of());

        assertThatThrownBy(() -> refeicaoService.create(dto, 1L))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void shouldThrowWhenCreatingWithNullFichas() {
        RefeicaoDTO dto = new RefeicaoDTO("Almoço", "500", Status.ATIVA, null);

        assertThatThrownBy(() -> refeicaoService.create(dto, 1L))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void shouldThrowWhenNutricionistaNotFound() {
        RefeicaoDTO dto = new RefeicaoDTO("Almoço", "500", Status.ATIVA, List.of(10L));

        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refeicaoService.create(dto, 99L))
                .isInstanceOf(UsuarioNotFoundException.class);
    }

    // =========================================================================
    // UPDATE
    // =========================================================================

    @Test
    void shouldUpdateRefeicaoSuccessfully() {
        Usuario nutricionista = criarUsuario(1L);
        Refeicao existing = criarRefeicao(5L, "Almoço Antigo");
        existing.setNutricionista(nutricionista);

        FichaTecnica ftExisting = criarFichaTecnica(10L);
        FichasPorRefeicao fpExisting = new FichasPorRefeicao();
        fpExisting.setRefeicao(existing);
        fpExisting.setFichaTecnica(ftExisting);
        fpExisting.setId(new FichasPorRefeicaoId(5L, 10L));
        existing.setFichasPorRefeicao(new ArrayList<>(List.of(fpExisting)));

        RefeicaoDTO dto = new RefeicaoDTO("Almoço Novo", "600", Status.ATIVA, List.of(10L, 20L));

        FichaTecnica ftNew = criarFichaTecnica(20L);

        when(refeicaoRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(refeicaoRepository.existsByNutricionistaIdAndNomeIgnoreCaseAndIdNot(1L, "Almoço Novo", 5L)).thenReturn(false);
        when(fichaTecnicaRepository.findAllById(any())).thenReturn(List.of(ftNew));
        when(refeicaoRepository.save(existing)).thenReturn(existing);

        RefeicaoResponseDTO result = refeicaoService.update(5L, dto);

        assertThat(result.nome()).isEqualTo("Almoço Novo");
        verify(refeicaoRepository).save(existing);
    }

    @Test
    void shouldThrowWhenUpdatingWithDuplicateName() {
        Usuario nutricionista = criarUsuario(1L);
        Refeicao existing = criarRefeicao(5L, "Almoço");
        existing.setNutricionista(nutricionista);
        existing.setFichasPorRefeicao(new ArrayList<>());

        RefeicaoDTO dto = new RefeicaoDTO("Jantar", "600", Status.ATIVA, List.of(10L));

        when(refeicaoRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(refeicaoRepository.existsByNutricionistaIdAndNomeIgnoreCaseAndIdNot(1L, "Jantar", 5L)).thenReturn(true);

        assertThatThrownBy(() -> refeicaoService.update(5L, dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Já existe outra refeição com este nome.");
    }

    @Test
    void shouldThrowWhenUpdatingNonExistentRefeicao() {
        RefeicaoDTO dto = new RefeicaoDTO("Jantar", "600", Status.ATIVA, List.of(10L));

        when(refeicaoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refeicaoService.update(999L, dto))
                .isInstanceOf(RefeicaoNotFoundException.class);
    }

    // =========================================================================
    // ATUALIZA STATUS
    // =========================================================================

    @Test
    void shouldToggleStatusFromAtivaToInativa() {
        Refeicao refeicao = criarRefeicao(1L, "Almoço");
        refeicao.setStatus(Status.ATIVA);

        when(refeicaoRepository.findById(1L)).thenReturn(Optional.of(refeicao));
        when(refeicaoRepository.save(refeicao)).thenReturn(refeicao);

        refeicaoService.atualizaStatus(1L);

        assertThat(refeicao.getStatus()).isEqualTo(Status.INATIVA);
        verify(refeicaoRepository).save(refeicao);
    }

    @Test
    void shouldToggleStatusFromInativaToAtiva() {
        Refeicao refeicao = criarRefeicao(1L, "Almoço");
        refeicao.setStatus(Status.INATIVA);

        when(refeicaoRepository.findById(1L)).thenReturn(Optional.of(refeicao));
        when(refeicaoRepository.save(refeicao)).thenReturn(refeicao);

        refeicaoService.atualizaStatus(1L);

        assertThat(refeicao.getStatus()).isEqualTo(Status.ATIVA);
        verify(refeicaoRepository).save(refeicao);
    }

    @Test
    void shouldThrowWhenToggleStatusForNonExistentRefeicao() {
        when(refeicaoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refeicaoService.atualizaStatus(99L))
                .isInstanceOf(RefeicaoNotFoundException.class);
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    private Refeicao criarRefeicao(Long id, String nome) {
        Refeicao r = new Refeicao();
        r.setId(id);
        r.setNome(nome);
        r.setKcalTotal("450");
        r.setStatus(Status.ATIVA);
        r.setFichasPorRefeicao(new ArrayList<>());
        return r;
    }

    private Usuario criarUsuario(Long id) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setUsername("usuario-" + id);
        u.setEmail("usuario" + id + "@exemplo.com");
        u.setSenha("senha");
        u.setAtivo(true);
        return u;
    }

    private FichaTecnica criarFichaTecnica(Long id) {
        Preparacao prep = new Preparacao();
        prep.setId(id);
        prep.setNome("Preparação " + id);

        PerfilNutricional pn = new PerfilNutricional();
        pn.setId(id);
        pn.setVtc(new BigDecimal("100.00"));

        FichaTecnica ft = new FichaTecnica();
        ft.setId(id);
        ft.setPreparacao(prep);
        ft.setPerfilNutricional(pn);
        return ft;
    }
}