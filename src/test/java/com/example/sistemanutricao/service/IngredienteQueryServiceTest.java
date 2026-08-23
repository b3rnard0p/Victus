package com.example.sistemanutricao.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.example.sistemanutricao.mapper.IngredienteMapper;
import com.example.sistemanutricao.model.Ingrediente;
import com.example.sistemanutricao.model.enuns.Status;
import com.example.sistemanutricao.model.Usuario;
import com.example.sistemanutricao.record.IngredienteDTO.IngredienteGetDTO;
import com.example.sistemanutricao.repository.IngredienteRepository;
import com.example.sistemanutricao.repository.UsuarioRepository;
import com.example.sistemanutricao.service.ficha.IngredienteTagClassifier;
import com.example.sistemanutricao.service.ingrediente.IngredienteQueryService;
import org.mapstruct.factory.Mappers;

@ExtendWith(MockitoExtension.class)
class IngredienteQueryServiceTest {

    @Mock
    private IngredienteRepository ingredienteRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private IngredienteTagClassifier ingredienteTagClassifier;

    private IngredienteQueryService queryService;

    @BeforeEach
    void setUp() {
        queryService = new IngredienteQueryService(
                ingredienteRepository,
                usuarioRepository,
                Mappers.getMapper(IngredienteMapper.class),
                ingredienteTagClassifier
        );
    }

    // =========================================================================
    // BUSCA POR STATUS E USUARIO
    // =========================================================================

    @Test
    void shouldReturnPageOfIngredientesByStatusAndUsuario() {
        Usuario usuario = criarUsuario(5L);
        Ingrediente ingrediente = criarIngrediente(10L, "Arroz", usuario);
        Pageable pageable = PageRequest.of(0, 6);
        Page<Ingrediente> page = new PageImpl<>(List.of(ingrediente), pageable, 1);

        when(ingredienteRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<IngredienteGetDTO> result = queryService.buscarPorStatusEUsuario(Status.ATIVA, 5L, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).nome()).isEqualTo("Arroz");
    }

    @Test
    void shouldReturnEmptyPageWhenUsuarioNotFound() {
        Pageable pageable = PageRequest.of(0, 6);

        when(ingredienteRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(Page.empty(pageable));
        Page<IngredienteGetDTO> result = queryService.buscarPorStatusEUsuario(Status.ATIVA, 99L, pageable);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void shouldReturnEmptyListWhenUsuarioNotFoundForListVersion() {
        when(ingredienteRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());
        List<IngredienteGetDTO> result = queryService.buscarPorStatusEUsuario(Status.ATIVA, 99L);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnListOfIngredientesByStatusAndUsuario() {
        Usuario usuario = criarUsuario(5L);
        Ingrediente ingrediente = criarIngrediente(10L, "Feijão", usuario);

        when(ingredienteRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(ingrediente)));

        List<IngredienteGetDTO> result = queryService.buscarPorStatusEUsuario(Status.ATIVA, 5L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).nome()).isEqualTo("Feijão");
    }

    // =========================================================================
    // PESQUISAR POR NOME
    // =========================================================================

    @Test
    void shouldSearchByNome() {
        Usuario usuario = criarUsuario(5L);
        Ingrediente ingrediente = criarIngrediente(10L, "Arroz Integral", usuario);
        Pageable pageable = PageRequest.of(0, 6);
        Page<Ingrediente> page = new PageImpl<>(List.of(ingrediente), pageable, 1);

        when(ingredienteRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        @SuppressWarnings("unchecked")
        Page<IngredienteGetDTO> result = (Page<IngredienteGetDTO>) queryService.pesquisar("nome", "Arroz", "especifico", 5L, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).nome()).isEqualTo("Arroz Integral");
    }

    @Test
    void shouldSearchByPtn() {
        Usuario usuario = criarUsuario(5L);
        Ingrediente ingrediente = criarIngrediente(10L, "Frango", usuario);
        ingrediente.setPtn(new BigDecimal("25.00"));
        Pageable pageable = PageRequest.of(0, 6);
        Page<Ingrediente> page = new PageImpl<>(List.of(ingrediente), pageable, 1);

        when(ingredienteRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        @SuppressWarnings("unchecked")
        Page<IngredienteGetDTO> result = (Page<IngredienteGetDTO>) queryService.pesquisar("ptn", "25.00", "especifico", 5L, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void shouldReturnEmptyPageForInvalidNumericField() {
        Pageable pageable = PageRequest.of(0, 6);

        Page<?> result = queryService.pesquisar("ptn", "abc", "especifico", 5L, pageable);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void shouldReturnEmptyPageForUnknownField() {
        Pageable pageable = PageRequest.of(0, 6);

        when(ingredienteRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(Page.empty(pageable));
        Page<?> result = queryService.pesquisar("desconhecido", "10", "especifico", 5L, pageable);

        assertThat(result.getContent()).isEmpty();
    }

    // =========================================================================
    // MULTIPLE USERS
    // =========================================================================

    @Test
    void shouldReturnIngredientesByStatusAndMultipleUsuarios() {
        Ingrediente ingrediente1 = criarIngrediente(1L, "Arroz", criarUsuario(1L));
        Ingrediente ingrediente2 = criarIngrediente(2L, "Feijao", criarUsuario(2L));
        
        when(ingredienteRepository.findByStatusAndUsuarioIdIn(eq(Status.ATIVA), any())).thenReturn(List.of(ingrediente1, ingrediente2));
        
        List<IngredienteGetDTO> result = queryService.buscarPorStatusEUsuarios(Status.ATIVA, 1L, 2L);
        assertThat(result).hasSize(2);
    }

    // =========================================================================
    // TACO SEARCHES
    // =========================================================================

    @Test
    void shouldReturnIngredientesDoUsuarioTaco() {
        Usuario tacoUser = criarUsuario(99L);
        when(usuarioRepository.findByUsernameIgnoreCase("TACO")).thenReturn(java.util.Optional.of(tacoUser));
        
        Pageable pageable = PageRequest.of(0, 6);
        Page<Ingrediente> page = new PageImpl<>(List.of(criarIngrediente(100L, "Taco Ing", tacoUser)), pageable, 1);
        when(ingredienteRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        
        Page<IngredienteGetDTO> result = queryService.buscarIngredientesDoUsuarioTaco(pageable);
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void shouldThrowExceptionWhenTacoUserNotFound() {
        when(usuarioRepository.findByUsernameIgnoreCase("TACO")).thenReturn(java.util.Optional.empty());
        
        org.junit.jupiter.api.Assertions.assertThrows(
            com.example.sistemanutricao.exception.UsuarioNotFoundException.class, 
            () -> queryService.buscarIngredientesDoUsuarioTaco(PageRequest.of(0, 10))
        );
    }

    @Test
    void shouldSearchTacoByNome() {
        Usuario tacoUser = criarUsuario(99L);
        when(usuarioRepository.findByUsernameIgnoreCase("TACO")).thenReturn(java.util.Optional.of(tacoUser));
        
        Pageable pageable = PageRequest.of(0, 6);
        Page<Ingrediente> page = new PageImpl<>(List.of(criarIngrediente(100L, "Maca", tacoUser)), pageable, 1);
        when(ingredienteRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        
        @SuppressWarnings("unchecked")
        Page<IngredienteGetDTO> result = (Page<IngredienteGetDTO>) queryService.pesquisarTaco("nome", "Maca", "especifico", pageable);
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void shouldSearchTacoInvalidNumber() {
        Usuario tacoUser = criarUsuario(99L);
        when(usuarioRepository.findByUsernameIgnoreCase("TACO")).thenReturn(java.util.Optional.of(tacoUser));
        
        Pageable pageable = PageRequest.of(0, 6);
        Page<?> result = queryService.pesquisarTaco("ptn", "abc", "especifico", pageable);
        assertThat(result.getContent()).isEmpty();
    }

    // =========================================================================
    // TAG SEARCHES
    // =========================================================================

    @Test
    void shouldSearchByTags() {
        Usuario usuario = criarUsuario(5L);
        Ingrediente ing1 = criarIngrediente(1L, "Arroz", usuario);
        Ingrediente ing2 = criarIngrediente(2L, "Feijao", usuario);
        
        Pageable pageable = PageRequest.of(0, 10);
        when(ingredienteRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(ing1, ing2)));
        when(ingredienteTagClassifier.determinarTag(ing1, "nome")).thenReturn("A");
        when(ingredienteTagClassifier.determinarTag(ing2, "nome")).thenReturn("B");
        
        @SuppressWarnings("unchecked")
        Page<com.example.sistemanutricao.record.IngredienteDTO.IngredienteComTagDTO> result = 
            (Page<com.example.sistemanutricao.record.IngredienteDTO.IngredienteComTagDTO>) queryService.pesquisar("nome", "A", "tags", 5L, pageable);
        
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).tag()).isEqualTo("A");
    }

    @Test
    void shouldSearchTacoByTags() {
        Usuario tacoUser = criarUsuario(99L);
        when(usuarioRepository.findByUsernameIgnoreCase("TACO")).thenReturn(java.util.Optional.of(tacoUser));
        
        Ingrediente ing1 = criarIngrediente(1L, "Arroz", tacoUser);
        
        Pageable pageable = PageRequest.of(0, 10);
        when(ingredienteRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(ing1)));
        when(ingredienteTagClassifier.determinarTag(ing1, "nome")).thenReturn("A");
        
        @SuppressWarnings("unchecked")
        Page<com.example.sistemanutricao.record.IngredienteDTO.IngredienteComTagDTO> result = 
            (Page<com.example.sistemanutricao.record.IngredienteDTO.IngredienteComTagDTO>) queryService.pesquisarTaco("nome", "A", "tags", pageable);
        
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void shouldPaginateListCorrectly() {
        Usuario usuario = criarUsuario(5L);
        Ingrediente ing1 = criarIngrediente(1L, "Arroz", usuario);
        Ingrediente ing2 = criarIngrediente(2L, "Feijao", usuario);
        
        when(ingredienteRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(ing1, ing2)));
        when(ingredienteTagClassifier.determinarTag(any(), any())).thenReturn("A");
        
        Page<com.example.sistemanutricao.record.IngredienteDTO.IngredienteComTagDTO> paged1 = queryService.buscarPorTag("nome", "A", 5L, PageRequest.of(0, 1));
        assertThat(paged1.getContent()).hasSize(1);
        assertThat(paged1.getTotalElements()).isEqualTo(2);
        
        Page<com.example.sistemanutricao.record.IngredienteDTO.IngredienteComTagDTO> paged2 = queryService.buscarPorTag("nome", "A", 5L, PageRequest.of(1, 1));
        assertThat(paged2.getContent()).hasSize(1);
        
        Page<com.example.sistemanutricao.record.IngredienteDTO.IngredienteComTagDTO> outOfBounds = queryService.buscarPorTag("nome", "A", 5L, PageRequest.of(5, 1));
        assertThat(outOfBounds.getContent()).isEmpty();
        
        Page<com.example.sistemanutricao.record.IngredienteDTO.IngredienteComTagDTO> unpaged = queryService.buscarPorTag("nome", "A", 5L, Pageable.unpaged());
        assertThat(unpaged.getContent()).hasSize(2);
        
        Page<com.example.sistemanutricao.record.IngredienteDTO.IngredienteComTagDTO> nullPage = queryService.buscarPorTag("nome", "A", 5L, null);
        assertThat(nullPage.getContent()).hasSize(2);
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    private Usuario criarUsuario(Long id) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setUsername("usuario-" + id);
        u.setEmail("usuario" + id + "@exemplo.com");
        u.setSenha("senha");
        u.setAtivo(true);
        return u;
    }

    private Ingrediente criarIngrediente(Long id, String nome, Usuario usuario) {
        Ingrediente i = new Ingrediente();
        i.setId(id);
        i.setNome(nome);
        i.setPtn(new BigDecimal("10.00"));
        i.setCho(new BigDecimal("20.00"));
        i.setLip(new BigDecimal("5.00"));
        i.setSodio(new BigDecimal("1.00"));
        i.setGorduraSaturada(new BigDecimal("0.50"));
        i.setStatus(Status.ATIVA);
        i.setUsuario(usuario);
        return i;
    }
}
