package com.example.sistemanutricao.service;

import com.example.sistemanutricao.mapper.FichaTecnicaMapper;
import com.example.sistemanutricao.model.FichaTecnica;
import com.example.sistemanutricao.model.Usuario;
import com.example.sistemanutricao.model.enuns.Status;
import com.example.sistemanutricao.model.enuns.StatusCriacao;
import com.example.sistemanutricao.record.FichaTecnicaDTO.FichaTecnicaGetDTO;
import com.example.sistemanutricao.repository.FichaTecnicaRepository;
import com.example.sistemanutricao.service.ficha.FichaQueryService;
import com.example.sistemanutricao.service.ficha.FichaTagClassifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FichaQueryServiceTest {

    @Mock
    private FichaTecnicaRepository fichaRepository;

    @Mock
    private FichaTagClassifier tagClassifier;

    @Mock
    private FichaTecnicaMapper fichaTecnicaMapper;

    @InjectMocks
    private FichaQueryService fichaQueryService;

    @Test
    void buscarPorStatusSimples_List() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        FichaTecnica f1 = new FichaTecnica();
        f1.setId(10L);

        when(fichaRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(f1)));

        FichaTecnicaGetDTO dto = new FichaTecnicaGetDTO(10L, null, null, null, null, null, null, null, null, null, null);
        when(fichaTecnicaMapper.toGetDTO(f1)).thenReturn(dto);

        List<FichaTecnicaGetDTO> res = fichaQueryService.buscarPorStatusSimples(Status.ATIVA, usuario);

        assertNotNull(res);
        assertEquals(1, res.size());
        assertEquals(10L, res.get(0).id());
    }

    @Test
    void buscarPorStatusSimples_Page() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        FichaTecnica f1 = new FichaTecnica();
        f1.setId(10L);

        PageRequest page = PageRequest.of(0, 10);
        when(fichaRepository.findAll(any(Specification.class), eq(page)))
                .thenReturn(new PageImpl<>(List.of(f1)));

        FichaTecnicaGetDTO dto = new FichaTecnicaGetDTO(10L, null, null, null, null, null, null, null, null, null, null);
        when(fichaTecnicaMapper.toGetDTO(f1)).thenReturn(dto);

        Page<FichaTecnicaGetDTO> res = fichaQueryService.buscarPorStatusSimples(Status.ATIVA, usuario, page);

        assertNotNull(res);
        assertEquals(1, res.getTotalElements());
        assertEquals(10L, res.getContent().get(0).id());
    }

    @Test
    void buscarPorStatus_List() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        FichaTecnica f1 = new FichaTecnica();
        f1.setId(10L);

        when(fichaRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(f1)));

        FichaTecnicaGetDTO dto = new FichaTecnicaGetDTO(10L, null, null, null, null, null, null, null, null, null, null);
        when(fichaTecnicaMapper.toGetDTO(f1)).thenReturn(dto);

        List<FichaTecnicaGetDTO> res = fichaQueryService.buscarPorStatus(Status.ATIVA, StatusCriacao.COMPLETA, usuario);

        assertNotNull(res);
        assertEquals(1, res.size());
        assertEquals(10L, res.get(0).id());
    }

    @Test
    void buscarPorStatus_Page() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        FichaTecnica f1 = new FichaTecnica();
        f1.setId(10L);

        PageRequest page = PageRequest.of(0, 10);
        when(fichaRepository.findAll(any(Specification.class), eq(page)))
                .thenReturn(new PageImpl<>(List.of(f1)));

        FichaTecnicaGetDTO dto = new FichaTecnicaGetDTO(10L, null, null, null, null, null, null, null, null, null, null);
        when(fichaTecnicaMapper.toGetDTO(f1)).thenReturn(dto);

        Page<FichaTecnicaGetDTO> res = fichaQueryService.buscarPorStatus(Status.ATIVA, StatusCriacao.INCOMPLETA, usuario, page);

        assertNotNull(res);
        assertEquals(1, res.getTotalElements());
        assertEquals(10L, res.getContent().get(0).id());
    }
    // =========================================================================
    // SEARCH TESTS
    // =========================================================================

    @Test
    void pesquisar_Tags() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        FichaTecnica f1 = new FichaTecnica();
        f1.setId(10L);
        f1.setPreparacao(new com.example.sistemanutricao.model.Preparacao());
        f1.setPerfilNutricional(new com.example.sistemanutricao.model.PerfilNutricional());
        f1.setNutricionista(usuario);

        PageRequest page = PageRequest.of(0, 10);
        when(fichaRepository.findAll(any(Specification.class), eq(page)))
                .thenReturn(new PageImpl<>(List.of(f1)));
        
        when(tagClassifier.determinarTag(eq(f1), anyString())).thenReturn("TAG_X");

        Page<?> res = fichaQueryService.pesquisar("nome", "TAG_X", "tags", usuario, page);
        assertNotNull(res);
    }

    @Test
    void pesquisar_DynamicFields() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setCargo(com.example.sistemanutricao.model.enuns.Cargo.NUTRICIONISTA);

        FichaTecnica f1 = new FichaTecnica();
        f1.setId(10L);

        PageRequest page = PageRequest.of(0, 10);
        when(fichaRepository.findAll(any(Specification.class), eq(page)))
                .thenReturn(new PageImpl<>(List.of(f1)));
        
        FichaTecnicaGetDTO dto = new FichaTecnicaGetDTO(10L, null, null, null, null, null, null, null, null, null, null);
        when(fichaTecnicaMapper.toGetDTO(f1)).thenReturn(dto);

        // Numero (valid)
        Page<?> res = fichaQueryService.pesquisar("por-numero", "123", "especifico", usuario, page);
        assertEquals(1, res.getTotalElements());

        // Numero (invalid)
        res = fichaQueryService.pesquisar("por-numero", "abc", "especifico", usuario, page);
        assertEquals(0, res.getTotalElements());

        // Categoria (valid)
        res = fichaQueryService.pesquisar("por-categoria", "Sobremesa", "especifico", usuario, page);
        assertEquals(1, res.getTotalElements());

        // Categoria (invalid)
        res = fichaQueryService.pesquisar("por-categoria", "Invalida", "especifico", usuario, page);
        assertEquals(0, res.getTotalElements());

        // Outros numericos (valid)
        res = fichaQueryService.pesquisar("ptn", "10.5", "especifico", usuario, page);
        assertEquals(1, res.getTotalElements());

        // Outros numericos (invalid)
        res = fichaQueryService.pesquisar("ptn", "xyz", "especifico", usuario, page);
        assertEquals(0, res.getTotalElements());
    }

    @Test
    void pesquisar_ProducaoUser() {
        Usuario usuario = new Usuario();
        usuario.setId(2L);
        usuario.setCargo(com.example.sistemanutricao.model.enuns.Cargo.PRODUCAO);
        com.example.sistemanutricao.model.Estabelecimento est = new com.example.sistemanutricao.model.Estabelecimento();
        est.setId(99L);
        usuario.setEstabelecimento(est);

        FichaTecnica f1 = new FichaTecnica();
        f1.setId(10L);

        PageRequest page = PageRequest.of(0, 10);
        when(fichaRepository.findAll(any(Specification.class), eq(page)))
                .thenReturn(new PageImpl<>(List.of(f1)));

        FichaTecnicaGetDTO dto = new FichaTecnicaGetDTO(10L, null, null, null, null, null, null, null, null, null, null);
        when(fichaTecnicaMapper.toGetDTO(f1)).thenReturn(dto);

        Page<?> res = fichaQueryService.pesquisar("por-nome", "Bolo", "especifico", usuario, page);
        assertEquals(1, res.getTotalElements());
    }

    @Test
    void buscarPorTag_List() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        FichaTecnica f1 = new FichaTecnica();
        f1.setId(10L);
        f1.setPreparacao(new com.example.sistemanutricao.model.Preparacao());
        f1.setPerfilNutricional(new com.example.sistemanutricao.model.PerfilNutricional());
        f1.setNutricionista(usuario);

        when(fichaRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(f1)));
        when(tagClassifier.determinarTag(eq(f1), anyString())).thenReturn("Alta");

        List<com.example.sistemanutricao.record.FichaTecnicaDTO.FichaTecnicaComTagDTO> res = fichaQueryService.buscarPorTag("ptn", "Alta", usuario);
        assertEquals(1, res.size());

        // Parametros nulos
        res = fichaQueryService.buscarPorTag(null, null, null);
        assertEquals(0, res.size());
    }

    @Test
    void buscarPorTag_Producao() {
        Usuario usuario = new Usuario();
        usuario.setId(2L);
        usuario.setCargo(com.example.sistemanutricao.model.enuns.Cargo.PRODUCAO);
        com.example.sistemanutricao.model.Estabelecimento est = new com.example.sistemanutricao.model.Estabelecimento();
        est.setId(99L);
        usuario.setEstabelecimento(est);

        FichaTecnica f1 = new FichaTecnica();
        f1.setId(10L);
        f1.setPreparacao(new com.example.sistemanutricao.model.Preparacao());
        f1.setPerfilNutricional(new com.example.sistemanutricao.model.PerfilNutricional());
        
        Usuario nutri = new Usuario(); nutri.setId(1L);
        f1.setNutricionista(nutri);

        when(fichaRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(f1)));
        when(tagClassifier.determinarTag(eq(f1), anyString())).thenReturn("Baixa");

        List<com.example.sistemanutricao.record.FichaTecnicaDTO.FichaTecnicaComTagDTO> res = fichaQueryService.buscarPorTag("cho", "Baixa", usuario);
        assertEquals(1, res.size());
    }

    @Test
    void exceptionInToComTagDTO() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        FichaTecnica f1 = new FichaTecnica();
        f1.setId(10L);
        // Sem preparacao etc.. NullPointerException inside toComTagDTO
        
        when(fichaRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(f1)));

        List<com.example.sistemanutricao.record.FichaTecnicaDTO.FichaTecnicaComTagDTO> res = fichaQueryService.buscarPorTag("ptn", "Alta", usuario);
        assertEquals(0, res.size());
    }
}
