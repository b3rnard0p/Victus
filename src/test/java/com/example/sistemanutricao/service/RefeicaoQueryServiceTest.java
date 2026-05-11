package com.example.sistemanutricao.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.example.sistemanutricao.mapper.RefeicaoMapper;
import com.example.sistemanutricao.model.enuns.Cargo;
import com.example.sistemanutricao.model.Estabelecimento;
import com.example.sistemanutricao.model.Refeicao;
import com.example.sistemanutricao.model.enuns.Status;
import com.example.sistemanutricao.model.Usuario;
import com.example.sistemanutricao.record.RefeicaoDTO.RefeicaoResponseDTO;
import com.example.sistemanutricao.repository.RefeicaoRepository;
import com.example.sistemanutricao.service.refeicao.RefeicaoQueryService;

@ExtendWith(MockitoExtension.class)
class RefeicaoQueryServiceTest {

    @Mock
    private RefeicaoRepository refeicaoRepository;

    @Mock
    private RefeicaoMapper refeicaoMapper;

    @InjectMocks
    private RefeicaoQueryService refeicaoQueryService;

    // =========================================================================
    // BUSCA POR STATUS
    // =========================================================================

    @Test
    void shouldReturnPageOfRefeicoesByStatusForNutricionista() {
        Usuario nutricionista = criarNutricionista(1L);
        Refeicao refeicao = criarRefeicao(10L, "Almoço");
        Pageable pageable = PageRequest.of(0, 6);
        Page<Refeicao> page = new PageImpl<>(List.of(refeicao), pageable, 1);
        RefeicaoResponseDTO dto = new RefeicaoResponseDTO(10L, "Almoço", "500", Status.ATIVA, List.of());

        when(refeicaoRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(refeicaoMapper.toResponseDTO(refeicao)).thenReturn(dto);

        Page<RefeicaoResponseDTO> result = refeicaoQueryService.buscarPorStatus(Status.ATIVA, nutricionista, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).nome()).isEqualTo("Almoço");
    }

    @Test
    void shouldReturnPageOfRefeicoesByStatusForProducao() {
        Usuario producao = criarProducao(2L, 50L);
        Refeicao refeicao = criarRefeicao(20L, "Jantar");
        Pageable pageable = PageRequest.of(0, 6);
        Page<Refeicao> page = new PageImpl<>(List.of(refeicao), pageable, 1);
        RefeicaoResponseDTO dto = new RefeicaoResponseDTO(20L, "Jantar", "600", Status.ATIVA, List.of());

        when(refeicaoRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(refeicaoMapper.toResponseDTO(refeicao)).thenReturn(dto);

        Page<RefeicaoResponseDTO> result = refeicaoQueryService.buscarPorStatus(Status.ATIVA, producao, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).nome()).isEqualTo("Jantar");
    }

    @Test
    void shouldReturnListOfRefeicoesByStatusForNutricionista() {
        Usuario nutricionista = criarNutricionista(1L);
        Refeicao refeicao = criarRefeicao(10L, "Almoço");
        RefeicaoResponseDTO dto = new RefeicaoResponseDTO(10L, "Almoço", "500", Status.ATIVA, List.of());

        when(refeicaoRepository.findAll(any(Specification.class), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(refeicao)));
        when(refeicaoMapper.toResponseDTO(refeicao)).thenReturn(dto);

        List<RefeicaoResponseDTO> result = refeicaoQueryService.buscarPorStatus(Status.ATIVA, nutricionista);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(10L);
    }

    @Test
    void shouldReturnEmptyListWhenNoRefeicoes() {
        Usuario nutricionista = criarNutricionista(1L);

        when(refeicaoRepository.findAll(any(Specification.class), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of()));

        List<RefeicaoResponseDTO> result = refeicaoQueryService.buscarPorStatus(Status.ATIVA, nutricionista);

        assertThat(result).isEmpty();
    }

    // =========================================================================
    // BUSCA POR NOME
    // =========================================================================

    @Test
    void shouldReturnPageByNomeForNutricionista() {
        Usuario nutricionista = criarNutricionista(1L);
        Refeicao refeicao = criarRefeicao(10L, "Café da Manhã");
        Pageable pageable = PageRequest.of(0, 6);
        Page<Refeicao> page = new PageImpl<>(List.of(refeicao), pageable, 1);
        RefeicaoResponseDTO dto = new RefeicaoResponseDTO(10L, "Café da Manhã", "300", Status.ATIVA, List.of());

        when(refeicaoRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(refeicaoMapper.toResponseDTO(refeicao)).thenReturn(dto);

        Page<RefeicaoResponseDTO> result = refeicaoQueryService.buscarPorNome("Café", nutricionista, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).nome()).isEqualTo("Café da Manhã");
    }

    @Test
    void shouldReturnPageByNomeForProducao() {
        Usuario producao = criarProducao(2L, 50L);
        Refeicao refeicao = criarRefeicao(20L, "Lanche");
        Pageable pageable = PageRequest.of(0, 6);
        Page<Refeicao> page = new PageImpl<>(List.of(refeicao), pageable, 1);
        RefeicaoResponseDTO dto = new RefeicaoResponseDTO(20L, "Lanche", "200", Status.ATIVA, List.of());

        when(refeicaoRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(refeicaoMapper.toResponseDTO(refeicao)).thenReturn(dto);

        Page<RefeicaoResponseDTO> result = refeicaoQueryService.buscarPorNome("Lanche", producao, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    private Refeicao criarRefeicao(Long id, String nome) {
        Refeicao r = new Refeicao();
        r.setId(id);
        r.setNome(nome);
        r.setKcalTotal("500");
        r.setStatus(Status.ATIVA);
        r.setFichasPorRefeicao(new ArrayList<>());
        return r;
    }

    private Usuario criarNutricionista(Long id) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setCargo(Cargo.NUTRICIONISTA);
        return u;
    }

    private Usuario criarProducao(Long id, Long estabelecimentoId) {
        Estabelecimento est = new Estabelecimento();
        est.setId(estabelecimentoId);

        Usuario u = new Usuario();
        u.setId(id);
        u.setCargo(Cargo.PRODUCAO);
        u.setEstabelecimento(est);
        return u;
    }
}
