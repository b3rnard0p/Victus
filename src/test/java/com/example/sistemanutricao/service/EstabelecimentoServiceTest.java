package com.example.sistemanutricao.service;

import com.example.sistemanutricao.exception.EstabelecimentoNotFoundException;
import com.example.sistemanutricao.model.Estabelecimento;
import com.example.sistemanutricao.model.Usuario;
import com.example.sistemanutricao.record.EstabelecimentoDTO.EstabelecimentoDTO;
import com.example.sistemanutricao.record.EstabelecimentoDTO.GetEstabelecimentoDTO;
import com.example.sistemanutricao.repository.EstabelecimentoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EstabelecimentoServiceTest {

    @Mock
    private EstabelecimentoRepository estabelecimentoRepository;

    @InjectMocks
    private EstabelecimentoService estabelecimentoService;

    @Test
    void create_Success() {
        EstabelecimentoDTO dto = new EstabelecimentoDTO(null, "Novo Estabelecimento");
        when(estabelecimentoRepository.existsByNome("Novo Estabelecimento")).thenReturn(false);

        Estabelecimento saved = new Estabelecimento();
        saved.setId(1L);
        saved.setNome("Novo Estabelecimento");
        when(estabelecimentoRepository.save(any(Estabelecimento.class))).thenReturn(saved);

        GetEstabelecimentoDTO result = estabelecimentoService.create(dto);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Novo Estabelecimento", result.nome());
        verify(estabelecimentoRepository).save(any(Estabelecimento.class));
    }

    @Test
    void create_AlreadyExists() {
        EstabelecimentoDTO dto = new EstabelecimentoDTO(null, "Existente");
        when(estabelecimentoRepository.existsByNome("Existente")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> estabelecimentoService.create(dto));
        verify(estabelecimentoRepository, never()).save(any());
    }

    @Test
    void findById_Success() {
        Estabelecimento est = new Estabelecimento();
        est.setId(1L);
        est.setNome("Teste");

        Usuario user = new Usuario();
        user.setId(2L);
        user.setEstabelecimento(est);
        est.setUsuario(List.of(user));

        when(estabelecimentoRepository.findById(1L)).thenReturn(Optional.of(est));

        GetEstabelecimentoDTO result = estabelecimentoService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals(1, result.usuarios().size());
        assertEquals(2L, result.usuarios().get(0).id());
        assertEquals(1L, result.usuarios().get(0).estabelecimentoId());
    }

    @Test
    void findById_NotFound() {
        when(estabelecimentoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EstabelecimentoNotFoundException.class, () -> estabelecimentoService.findById(1L));
    }

    @Test
    void update_Success() {
        EstabelecimentoDTO dto = new EstabelecimentoDTO(null, "Atualizado");
        when(estabelecimentoRepository.existsByNomeAndIdNot("Atualizado", 1L)).thenReturn(false);

        Estabelecimento est = new Estabelecimento();
        est.setId(1L);
        est.setNome("Antigo");

        when(estabelecimentoRepository.findById(1L)).thenReturn(Optional.of(est));
        when(estabelecimentoRepository.save(any(Estabelecimento.class))).thenReturn(est);

        GetEstabelecimentoDTO result = estabelecimentoService.update(1L, dto);

        assertNotNull(result);
        assertEquals("Atualizado", result.nome());
        verify(estabelecimentoRepository).save(est);
    }

    @Test
    void update_AlreadyExists() {
        EstabelecimentoDTO dto = new EstabelecimentoDTO(null, "Existente");
        when(estabelecimentoRepository.existsByNomeAndIdNot("Existente", 1L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> estabelecimentoService.update(1L, dto));
        verify(estabelecimentoRepository, never()).save(any());
    }
    
    @Test
    void listAll() {
        Estabelecimento est = new Estabelecimento();
        est.setId(1L);
        est.setNome("Teste");
        
        when(estabelecimentoRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(est)));
        
        List<GetEstabelecimentoDTO> list = estabelecimentoService.listAll();
        assertEquals(1, list.size());
    }
}
