package com.example.sistemanutricao.service;

import com.example.sistemanutricao.exception.FichaTecnicaNotFoundException;
import com.example.sistemanutricao.exception.UsuarioNotFoundException;
import com.example.sistemanutricao.mapper.FichaTecnicaMapper;
import com.example.sistemanutricao.model.FichaTecnica;
import com.example.sistemanutricao.model.Usuario;
import com.example.sistemanutricao.model.enuns.Status;
import com.example.sistemanutricao.model.enuns.StatusCriacao;
import com.example.sistemanutricao.record.FichaTecnicaDTO.FichaTecnicaCreateDTO;
import com.example.sistemanutricao.record.FichaTecnicaDTO.FichaTecnicaGetDTO;
import com.example.sistemanutricao.repository.FichaTecnicaRepository;
import com.example.sistemanutricao.repository.IngredienteRepository;
import com.example.sistemanutricao.repository.UsuarioRepository;
import com.example.sistemanutricao.service.ficha.FichaTecnicaService;
import com.example.sistemanutricao.service.ficha.PerfilNutricionalCalculator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FichaTecnicaServiceTest {

    @Mock
    private FichaTecnicaRepository fichaRepository;

    @Mock
    private IngredienteRepository ingredienteRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PerfilNutricionalCalculator perfilCalculator;

    @Mock
    private FichaTecnicaMapper fichaTecnicaMapper;

    @InjectMocks
    private FichaTecnicaService fichaTecnicaService;

    @Test
    void getFichaById_Success() {
        FichaTecnica ficha = new FichaTecnica();
        ficha.setId(1L);
        when(fichaRepository.findById(1L)).thenReturn(Optional.of(ficha));
        
        FichaTecnicaGetDTO dto = new FichaTecnicaGetDTO(1L, null, null, null, null, null, null, null, null, null, null);
        when(fichaTecnicaMapper.toGetDTO(ficha)).thenReturn(dto);

        FichaTecnicaGetDTO result = fichaTecnicaService.getFichaById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
    }

    @Test
    void getFichaById_NotFound() {
        when(fichaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(FichaTecnicaNotFoundException.class, () -> 
            fichaTecnicaService.getFichaById(1L)
        );
    }

    @Test
    void atualizaStatus_Success() {
        FichaTecnica ficha = new FichaTecnica();
        ficha.setStatus(Status.ATIVA);
        ficha.setStatusCriacao(StatusCriacao.COMPLETA);
        when(fichaRepository.findById(1L)).thenReturn(Optional.of(ficha));
        when(fichaRepository.save(any(FichaTecnica.class))).thenReturn(ficha);

        fichaTecnicaService.atualizaStatus(1L);

        assertEquals(Status.INATIVA, ficha.getStatus());
        verify(fichaRepository).save(ficha);
    }

    @Test
    void atualizaStatus_Exception_Incompleta() {
        FichaTecnica ficha = new FichaTecnica();
        ficha.setStatus(Status.ATIVA);
        ficha.setStatusCriacao(StatusCriacao.INCOMPLETA);
        when(fichaRepository.findById(1L)).thenReturn(Optional.of(ficha));

        assertThrows(IllegalArgumentException.class, () -> fichaTecnicaService.atualizaStatus(1L));
    }

    // =========================================================================
    // CREATE
    // =========================================================================

    @Test
    void create_NutricionistaNotFound() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UsuarioNotFoundException.class, () -> 
            fichaTecnicaService.create(null, 1L)
        );
    }

    @Test
    void create_StatusIncompletaEInativa() {
        Usuario nutricionista = new Usuario();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(nutricionista));

        FichaTecnicaCreateDTO dto = new FichaTecnicaCreateDTO(
            null, null, BigDecimal.ONE, "x", null, BigDecimal.ONE, Status.INATIVA, StatusCriacao.INCOMPLETA, null, null, null, null, null
        );

        assertThrows(IllegalArgumentException.class, () -> fichaTecnicaService.create(dto, 1L));
    }

    @Test
    void create_AguaInvalida() {
        Usuario nutricionista = new Usuario();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(nutricionista));

        com.example.sistemanutricao.record.PreparacaoDTO.PreparacaoDTO prepDto = 
            new com.example.sistemanutricao.record.PreparacaoDTO.PreparacaoDTO(null, "Bolo", 1, "30m", "Forno", "Modo", BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.TEN, null);

        FichaTecnicaCreateDTO dto = new FichaTecnicaCreateDTO(
            null, null, BigDecimal.ONE, "Fatia", null, BigDecimal.TEN, Status.ATIVA, StatusCriacao.COMPLETA, null, null, prepDto, null, null
        );

        // Apenas um dos campos de agua esta preenchido (ou <= 0)
        assertThrows(IllegalArgumentException.class, () -> fichaTecnicaService.create(dto, 1L));
    }

    @Test
    void create_Success() {
        Usuario nutricionista = new Usuario();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(nutricionista));

        com.example.sistemanutricao.record.PreparacaoDTO.PreparacaoDTO prepDto = 
            new com.example.sistemanutricao.record.PreparacaoDTO.PreparacaoDTO(null, "Bolo", 1, "30m", "Fogo", "Modo", BigDecimal.valueOf(100), BigDecimal.ONE, BigDecimal.ONE, null, null);

        com.example.sistemanutricao.record.IngredientePorFichaDTO ingDto = 
            new com.example.sistemanutricao.record.IngredientePorFichaDTO(null, 10L, null, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, "x", BigDecimal.ONE, BigDecimal.ONE, null, null, null, null, null, null);
        
        FichaTecnicaCreateDTO dto = new FichaTecnicaCreateDTO(
            null, null, BigDecimal.valueOf(100), "Fatia", null, BigDecimal.TEN, Status.ATIVA, StatusCriacao.COMPLETA, null, null, prepDto, List.of(ingDto), null
        );

        com.example.sistemanutricao.model.Preparacao prep = new com.example.sistemanutricao.model.Preparacao();
        prep.setRendimento(BigDecimal.valueOf(1000));
        when(fichaTecnicaMapper.toPreparacao(prepDto)).thenReturn(prep);

        com.example.sistemanutricao.model.PerfilNutricional perfil = new com.example.sistemanutricao.model.PerfilNutricional();
        when(fichaTecnicaMapper.toPerfilNutricional(any())).thenReturn(perfil);

        com.example.sistemanutricao.model.Ingrediente ing = new com.example.sistemanutricao.model.Ingrediente();
        ing.setId(10L);
        ing.setPtn(BigDecimal.TEN);
        when(ingredienteRepository.findById(10L)).thenReturn(Optional.of(ing));

        when(fichaRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        FichaTecnicaGetDTO resultDto = new FichaTecnicaGetDTO(1L, null, null, null, null, null, null, null, null, null, null);
        when(fichaTecnicaMapper.toGetDTO(any())).thenReturn(resultDto);

        FichaTecnicaGetDTO res = fichaTecnicaService.create(dto, 1L);
        assertNotNull(res);
    }

    // =========================================================================
    // UPDATE
    // =========================================================================

    @Test
    void update_NotFound() {
        when(fichaRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(FichaTecnicaNotFoundException.class, () -> fichaTecnicaService.update(1L, null));
    }

    @Test
    void update_Success_SyncIngredientes() {
        FichaTecnica ficha = new FichaTecnica();
        ficha.setId(1L);
        ficha.setNutricionista(new Usuario());
        
        com.example.sistemanutricao.model.Preparacao prep = new com.example.sistemanutricao.model.Preparacao();
        prep.setRendimento(BigDecimal.valueOf(1000));
        ficha.setPreparacao(prep);

        com.example.sistemanutricao.model.IngredientesPorFicha ipf = new com.example.sistemanutricao.model.IngredientesPorFicha();
        ipf.setId(100L);
        ipf.setIngrediente(new com.example.sistemanutricao.model.Ingrediente());
        List<com.example.sistemanutricao.model.IngredientesPorFicha> ipfs = new ArrayList<>();
        ipfs.add(ipf);
        ficha.setIngredientesPorFicha(ipfs);

        when(fichaRepository.findById(1L)).thenReturn(Optional.of(ficha));

        com.example.sistemanutricao.record.PreparacaoDTO.PreparacaoDTO prepDto = 
            new com.example.sistemanutricao.record.PreparacaoDTO.PreparacaoDTO(null, "Bolo", 1, "1h", "Fogo", "Modo", BigDecimal.valueOf(100), BigDecimal.ONE, BigDecimal.ONE, null, null);

        // Update with one existing, one new
        com.example.sistemanutricao.record.IngredientePorFichaDTO ingExist = 
            new com.example.sistemanutricao.record.IngredientePorFichaDTO(100L, 10L, null, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, "x", BigDecimal.ONE, BigDecimal.ONE, null, null, null, null, null, null);
        com.example.sistemanutricao.record.IngredientePorFichaDTO ingNew = 
            new com.example.sistemanutricao.record.IngredientePorFichaDTO(null, 11L, null, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, "x", BigDecimal.ONE, BigDecimal.ONE, null, null, null, null, null, null);
        
        com.example.sistemanutricao.record.FichaTecnicaDTO.FichaTecnicaUpdateDTO dto = new com.example.sistemanutricao.record.FichaTecnicaDTO.FichaTecnicaUpdateDTO(
            null, null, BigDecimal.valueOf(100), "Fatia", null, BigDecimal.TEN, Status.ATIVA, StatusCriacao.COMPLETA, prepDto, List.of(ingExist, ingNew), null
        );

        com.example.sistemanutricao.model.Ingrediente ing = new com.example.sistemanutricao.model.Ingrediente();
        ing.setId(11L);
        ing.setPtn(BigDecimal.TEN);
        when(ingredienteRepository.findById(11L)).thenReturn(Optional.of(ing));

        when(fichaRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        FichaTecnicaGetDTO resultDto = new FichaTecnicaGetDTO(1L, null, null, null, null, null, null, null, null, null, null);
        when(fichaTecnicaMapper.toGetDTO(any())).thenReturn(resultDto);

        FichaTecnicaGetDTO res = fichaTecnicaService.update(1L, dto);
        assertNotNull(res);
        assertEquals(2, ficha.getIngredientesPorFicha().size());
    }

    @Test
    void listarResumo() {
        FichaTecnica ficha = new FichaTecnica();
        ficha.setId(1L);
        ficha.setPreparacao(new com.example.sistemanutricao.model.Preparacao());
        ficha.setPerfilNutricional(new com.example.sistemanutricao.model.PerfilNutricional());

        when(fichaRepository.findByStatusAndStatusCriacao(eq(Status.ATIVA), eq(StatusCriacao.COMPLETA), any()))
            .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(ficha)));

        List<?> resumo = fichaTecnicaService.listarResumo();
        assertEquals(1, resumo.size());
    }
}
