package com.example.sistemanutricao.service.taco;

import com.example.sistemanutricao.model.Ingrediente;
import com.example.sistemanutricao.model.Usuario;
import com.example.sistemanutricao.repository.IngredienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class TacoIngredientePersisterTest {

    @Mock
    private IngredienteRepository ingredienteRepository;

    @Mock
    private TacoExcelParser tacoExcelParser;

    @InjectMocks
    private TacoIngredientePersister persister;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setUsername("TACO");
    }

    @Test
    void testPersistirNovosIngredientes() {
        when(ingredienteRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        when(tacoExcelParser.normalizarNome("Arroz")).thenReturn("arroz");

        TacoIngredienteLinha linha1 = new TacoIngredienteLinha("Arroz", null, null, null, null);
        List<TacoIngredienteLinha> linhas = List.of(linha1);
        Map<String, BigDecimal> saturadas = Map.of("arroz", new BigDecimal("0.5"));

        persister.persistir(usuario, linhas, saturadas);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Ingrediente>> captor = ArgumentCaptor.forClass(List.class);
        verify(ingredienteRepository).saveAll(captor.capture());

        List<Ingrediente> salvos = captor.getValue();
        assertThat(salvos).hasSize(1);
        Ingrediente salvo = salvos.get(0);
        assertThat(salvo.getNome()).isEqualTo("Arroz");
        assertThat(salvo.getPtn()).isEqualTo(BigDecimal.ZERO);
        assertThat(salvo.getCho()).isEqualTo(BigDecimal.ZERO);
        assertThat(salvo.getLip()).isEqualTo(BigDecimal.ZERO);
        assertThat(salvo.getSodio()).isEqualTo(BigDecimal.ZERO);
        assertThat(salvo.getGorduraSaturada()).isEqualTo(new BigDecimal("0.5"));
        assertThat(salvo.getUsuario()).isEqualTo(usuario);
    }

    @Test
    void testAtualizarIngredienteExistenteComNovaGorduraSaturada() {
        Ingrediente existente = new Ingrediente();
        existente.setNome("Arroz");
        existente.setGorduraSaturada(new BigDecimal("0.0"));
        
        when(ingredienteRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(existente)));

        when(tacoExcelParser.normalizarNome("Arroz")).thenReturn("arroz");

        TacoIngredienteLinha linha1 = new TacoIngredienteLinha("Arroz", new BigDecimal("2.0"), new BigDecimal("30.0"), new BigDecimal("1.0"), new BigDecimal("5.0"));
        List<TacoIngredienteLinha> linhas = List.of(linha1);
        Map<String, BigDecimal> saturadas = Map.of("arroz", new BigDecimal("0.5"));

        persister.persistir(usuario, linhas, saturadas);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Ingrediente>> captor = ArgumentCaptor.forClass(List.class);
        verify(ingredienteRepository).saveAll(captor.capture());

        List<Ingrediente> salvos = captor.getValue();
        assertThat(salvos).hasSize(1);
        assertThat(salvos.get(0).getGorduraSaturada()).isEqualTo(new BigDecimal("0.5"));
    }
}
