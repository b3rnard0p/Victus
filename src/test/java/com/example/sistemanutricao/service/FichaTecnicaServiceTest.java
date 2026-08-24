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
import com.example.sistemanutricao.record.FichaTecnicaDTO.FichaTecnicaUpdateDTO;
import com.example.sistemanutricao.exception.DuplicateNomeException;
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
            new com.example.sistemanutricao.record.PreparacaoDTO.PreparacaoDTO(null, "Bolo", 1, "30m", "Fogo", "Modo", null, null, BigDecimal.ONE, BigDecimal.valueOf(100), null);

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
            new com.example.sistemanutricao.record.PreparacaoDTO.PreparacaoDTO(null, "Bolo", 1, "1h", "Fogo", "Modo", null, null, BigDecimal.ONE, BigDecimal.valueOf(100), null);

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

    // =========================================================================
    // DUPLICIDADE CREATE/UPDATE
    // =========================================================================

    @Test
    void create_DuplicateNome() {
        Usuario nutricionista = new Usuario();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(nutricionista));

        com.example.sistemanutricao.record.PreparacaoDTO.PreparacaoDTO prepDto = 
            new com.example.sistemanutricao.record.PreparacaoDTO.PreparacaoDTO(null, "Bolo", 1, "30m", "Fogo", "Modo", null, null, BigDecimal.ONE, BigDecimal.valueOf(100), null);
        
        FichaTecnicaCreateDTO dto = new FichaTecnicaCreateDTO(
            null, null, BigDecimal.valueOf(100), "Fatia", null, BigDecimal.TEN, Status.ATIVA, StatusCriacao.COMPLETA, null, null, prepDto, List.of(), null
        );

        when(fichaRepository.existsByNutricionistaIdAndPreparacaoNomeIgnoreCase(1L, "Bolo")).thenReturn(true);

        assertThrows(DuplicateNomeException.class, () -> fichaTecnicaService.create(dto, 1L));
    }

    @Test
    void create_DuplicateNumero() {
        Usuario nutricionista = new Usuario();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(nutricionista));

        com.example.sistemanutricao.record.PreparacaoDTO.PreparacaoDTO prepDto = 
            new com.example.sistemanutricao.record.PreparacaoDTO.PreparacaoDTO(null, "Bolo", 1, "30m", "Fogo", "Modo", null, null, BigDecimal.ONE, BigDecimal.valueOf(100), null);
        
        FichaTecnicaCreateDTO dto = new FichaTecnicaCreateDTO(
            null, null, BigDecimal.valueOf(100), "Fatia", null, BigDecimal.TEN, Status.ATIVA, StatusCriacao.COMPLETA, null, null, prepDto, List.of(), null
        );

        when(fichaRepository.existsByNutricionistaIdAndPreparacaoNomeIgnoreCase(1L, "Bolo")).thenReturn(false);
        when(fichaRepository.existsByNutricionistaIdAndPreparacaoNumero(1L, 1)).thenReturn(true);

        assertThrows(DuplicateNomeException.class, () -> fichaTecnicaService.create(dto, 1L));
    }

    @Test
    void update_DuplicateNome() {
        FichaTecnica ficha = new FichaTecnica();
        ficha.setId(1L);
        Usuario nutricionista = new Usuario();
        nutricionista.setId(2L);
        ficha.setNutricionista(nutricionista);
        
        com.example.sistemanutricao.model.Preparacao prep = new com.example.sistemanutricao.model.Preparacao();
        prep.setRendimento(BigDecimal.valueOf(1000));
        ficha.setPreparacao(prep);

        when(fichaRepository.findById(1L)).thenReturn(Optional.of(ficha));

        com.example.sistemanutricao.record.PreparacaoDTO.PreparacaoDTO prepDto = 
            new com.example.sistemanutricao.record.PreparacaoDTO.PreparacaoDTO(null, "Bolo 2", 2, "30m", "Fogo", "Modo", null, null, BigDecimal.ONE, BigDecimal.valueOf(100), null);
        
        FichaTecnicaUpdateDTO dto = new FichaTecnicaUpdateDTO(
            null, null, BigDecimal.valueOf(100), "Fatia", null, BigDecimal.TEN, Status.ATIVA, StatusCriacao.COMPLETA, prepDto, List.of(), null
        );

        when(fichaRepository.existsByNutricionistaIdAndPreparacaoNomeIgnoreCaseAndIdNot(2L, "Bolo 2", 1L)).thenReturn(true);

        assertThrows(DuplicateNomeException.class, () -> fichaTecnicaService.update(1L, dto));
    }

    @Test
    void update_DuplicateNumero() {
        FichaTecnica ficha = new FichaTecnica();
        ficha.setId(1L);
        Usuario nutricionista = new Usuario();
        nutricionista.setId(2L);
        ficha.setNutricionista(nutricionista);
        
        com.example.sistemanutricao.model.Preparacao prep = new com.example.sistemanutricao.model.Preparacao();
        prep.setRendimento(BigDecimal.valueOf(1000));
        ficha.setPreparacao(prep);

        when(fichaRepository.findById(1L)).thenReturn(Optional.of(ficha));

        com.example.sistemanutricao.record.PreparacaoDTO.PreparacaoDTO prepDto = 
            new com.example.sistemanutricao.record.PreparacaoDTO.PreparacaoDTO(null, "Bolo", 2, "30m", "Fogo", "Modo", BigDecimal.valueOf(100), null, null, null, null);
        
        FichaTecnicaUpdateDTO dto = new FichaTecnicaUpdateDTO(
            null, null, BigDecimal.valueOf(100), "Fatia", null, BigDecimal.TEN, Status.ATIVA, StatusCriacao.COMPLETA, prepDto, List.of(), null
        );

        when(fichaRepository.existsByNutricionistaIdAndPreparacaoNomeIgnoreCaseAndIdNot(2L, "Bolo", 1L)).thenReturn(false);
        when(fichaRepository.existsByNutricionistaIdAndPreparacaoNumeroAndIdNot(2L, 2, 1L)).thenReturn(true);

        assertThrows(DuplicateNomeException.class, () -> fichaTecnicaService.update(1L, dto));
    }

    // =========================================================================
    // CALCULOS AGUA / PL / PORCOES
    // =========================================================================

    @Test
    void create_AguaZeradaOuNegativa() {
        Usuario nutricionista = new Usuario();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(nutricionista));

        com.example.sistemanutricao.record.PreparacaoDTO.PreparacaoDTO prepDto = 
            new com.example.sistemanutricao.record.PreparacaoDTO.PreparacaoDTO(null, "Sopa", 1, "30m", "Fogo", "Modo", BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.valueOf(100), null);

        FichaTecnicaCreateDTO dto = new FichaTecnicaCreateDTO(
            null, null, BigDecimal.valueOf(100), "Prato", null, BigDecimal.TEN, Status.ATIVA, StatusCriacao.COMPLETA, null, null, prepDto, List.of(), null
        );

        assertThrows(IllegalArgumentException.class, () -> fichaTecnicaService.create(dto, 1L));
    }

    @Test
    void create_PLZero_Success() {
        Usuario nutricionista = new Usuario();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(nutricionista));

        com.example.sistemanutricao.record.PreparacaoDTO.PreparacaoDTO prepDto = 
            new com.example.sistemanutricao.record.PreparacaoDTO.PreparacaoDTO(null, "Bolo", 1, "30m", "Fogo", "Modo", null, null, BigDecimal.ONE, BigDecimal.valueOf(100), null);

        // PL Zero
        com.example.sistemanutricao.record.IngredientePorFichaDTO ingDto = 
            new com.example.sistemanutricao.record.IngredientePorFichaDTO(null, 10L, null, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ZERO, "x", BigDecimal.ONE, BigDecimal.ONE, null, null, null, null, null, null);
        
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
        when(ingredienteRepository.findById(10L)).thenReturn(Optional.of(ing));

        when(fichaRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(fichaTecnicaMapper.toGetDTO(any())).thenReturn(new FichaTecnicaGetDTO(1L, null, null, null, null, null, null, null, null, null, null));

        FichaTecnicaGetDTO res = fichaTecnicaService.create(dto, 1L);
        assertNotNull(res);
    }

    @Test
    void create_InativaIncompleta_ThrowsException() {
        Usuario nutricionista = new Usuario();
        nutricionista.setId(1L);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(nutricionista));

        com.example.sistemanutricao.record.PreparacaoDTO.PreparacaoDTO prepDto = 
            new com.example.sistemanutricao.record.PreparacaoDTO.PreparacaoDTO(null, "Teste", 1, "30m", "Fogo", "Modo", null, null, BigDecimal.ONE, BigDecimal.TEN, null);
        
        FichaTecnicaCreateDTO dto = new FichaTecnicaCreateDTO(
            null, null, BigDecimal.valueOf(100), "Prato", null, BigDecimal.TEN, Status.INATIVA, StatusCriacao.INCOMPLETA, null, null, prepDto, List.of(), null
        );

        assertThrows(IllegalArgumentException.class, () -> fichaTecnicaService.create(dto, 1L));
    }

    @Test
    void update_InativaIncompleta_ThrowsException() {
        FichaTecnica ficha = new FichaTecnica();
        ficha.setId(1L);
        when(fichaRepository.findById(1L)).thenReturn(Optional.of(ficha));

        com.example.sistemanutricao.record.PreparacaoDTO.PreparacaoDTO prepDto = 
            new com.example.sistemanutricao.record.PreparacaoDTO.PreparacaoDTO(null, "Teste", 1, "30m", "Fogo", "Modo", null, null, BigDecimal.ONE, BigDecimal.TEN, null);
        
        FichaTecnicaUpdateDTO dto = new FichaTecnicaUpdateDTO(
            null, null, BigDecimal.valueOf(100), "Prato", null, BigDecimal.TEN, Status.INATIVA, StatusCriacao.INCOMPLETA, prepDto, List.of(), null
        );

        assertThrows(IllegalArgumentException.class, () -> fichaTecnicaService.update(1L, dto));
    }

    @Test
    void update_StatusNull_UsesExisting() {
        FichaTecnica ficha = new FichaTecnica();
        ficha.setId(1L);
        ficha.setStatus(Status.ATIVA);
        ficha.setStatusCriacao(StatusCriacao.COMPLETA);
        
        com.example.sistemanutricao.model.Preparacao p = new com.example.sistemanutricao.model.Preparacao();
        p.setRendimento(BigDecimal.TEN);
        ficha.setPreparacao(p);
        
        when(fichaRepository.findById(1L)).thenReturn(Optional.of(ficha));

        com.example.sistemanutricao.record.PreparacaoDTO.PreparacaoDTO prepDto = 
            new com.example.sistemanutricao.record.PreparacaoDTO.PreparacaoDTO(null, "Teste", 1, "30m", "Fogo", "Modo", null, null, BigDecimal.ONE, BigDecimal.TEN, null);
        
        FichaTecnicaUpdateDTO dto = new FichaTecnicaUpdateDTO(
            null, null, BigDecimal.valueOf(100), "Prato", null, BigDecimal.TEN, null, null, prepDto, List.of(), null
        );

        when(fichaTecnicaMapper.toGetDTO(any())).thenReturn(new FichaTecnicaGetDTO(1L, null, null, null, null, null, null, null, null, null, null));
        when(fichaRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        assertNotNull(fichaTecnicaService.update(1L, dto));
    }

    @Test
    void create_NullDTOs() {
        Usuario nutricionista = new Usuario();
        nutricionista.setId(1L);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(nutricionista));

        // Test with null preparacao to hit early return in duplicidade
        FichaTecnicaCreateDTO dto = new FichaTecnicaCreateDTO(
            null, null, BigDecimal.valueOf(100), "Prato", null, BigDecimal.TEN, Status.ATIVA, StatusCriacao.COMPLETA, null, null, null, null, null
        );

        assertThrows(NullPointerException.class, () -> fichaTecnicaService.create(dto, 1L));
    }

    @Test
    void update_NullDTOs() {
        FichaTecnica ficha = new FichaTecnica();
        ficha.setId(1L);
        when(fichaRepository.findById(1L)).thenReturn(Optional.of(ficha));

        FichaTecnicaUpdateDTO dto = new FichaTecnicaUpdateDTO(
            null, null, BigDecimal.valueOf(100), "Prato", null, BigDecimal.TEN, Status.ATIVA, StatusCriacao.COMPLETA, null, null, null
        );

        assertThrows(NullPointerException.class, () -> fichaTecnicaService.update(1L, dto));
    }

    @Test
    void create_RendimentoZero_ThrowsException() {
        Usuario nutricionista = new Usuario();
        nutricionista.setId(1L);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(nutricionista));

        com.example.sistemanutricao.record.PreparacaoDTO.PreparacaoDTO prepDto = 
            new com.example.sistemanutricao.record.PreparacaoDTO.PreparacaoDTO(null, "Teste", 1, "30m", "Fogo", "Modo", null, null, BigDecimal.ONE, BigDecimal.ZERO, null);
        
        com.example.sistemanutricao.model.Preparacao prep = new com.example.sistemanutricao.model.Preparacao();
        prep.setRendimento(BigDecimal.ZERO);
        when(fichaTecnicaMapper.toPreparacao(prepDto)).thenReturn(prep);

        FichaTecnicaCreateDTO dto = new FichaTecnicaCreateDTO(
            null, null, BigDecimal.TEN, "Prato", null, BigDecimal.TEN, Status.ATIVA, StatusCriacao.COMPLETA, null, null, prepDto, List.of(), null
        );

        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> fichaTecnicaService.create(dto, 1L));
    }

    @Test
    void create_PesoPorcaoZero_ThrowsException() {
        Usuario nutricionista = new Usuario();
        nutricionista.setId(1L);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(nutricionista));

        com.example.sistemanutricao.record.PreparacaoDTO.PreparacaoDTO prepDto = 
            new com.example.sistemanutricao.record.PreparacaoDTO.PreparacaoDTO(null, "Teste", 1, "30m", "Fogo", "Modo", null, null, BigDecimal.ONE, BigDecimal.TEN, null);
        
        com.example.sistemanutricao.model.Preparacao prep = new com.example.sistemanutricao.model.Preparacao();
        prep.setRendimento(BigDecimal.ONE);
        when(fichaTecnicaMapper.toPreparacao(prepDto)).thenReturn(prep);

        FichaTecnicaCreateDTO dto = new FichaTecnicaCreateDTO(
            null, null, BigDecimal.TEN, "Prato", null, BigDecimal.ZERO, Status.ATIVA, StatusCriacao.COMPLETA, null, null, prepDto, List.of(), null
        );

        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> fichaTecnicaService.create(dto, 1L));
    }

    @Test
    void create_RendimentoNull_ThrowsException() {
        Usuario nutricionista = new Usuario();
        nutricionista.setId(1L);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(nutricionista));

        com.example.sistemanutricao.record.PreparacaoDTO.PreparacaoDTO prepDto = 
            new com.example.sistemanutricao.record.PreparacaoDTO.PreparacaoDTO(null, "Teste", 1, "30m", "Fogo", "Modo", null, null, BigDecimal.ONE, null, null);
        
        com.example.sistemanutricao.model.Preparacao prep = new com.example.sistemanutricao.model.Preparacao();
        prep.setRendimento(null);
        when(fichaTecnicaMapper.toPreparacao(prepDto)).thenReturn(prep);

        FichaTecnicaCreateDTO dto = new FichaTecnicaCreateDTO(
            null, null, BigDecimal.TEN, "Prato", null, BigDecimal.TEN, Status.ATIVA, StatusCriacao.COMPLETA, null, null, prepDto, List.of(), null
        );

        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> fichaTecnicaService.create(dto, 1L));
    }

    @Test
    void create_PorcoesMenorQueUm_ThrowsException() {
        Usuario nutricionista = new Usuario();
        nutricionista.setId(1L);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(nutricionista));

        com.example.sistemanutricao.record.PreparacaoDTO.PreparacaoDTO prepDto = 
            new com.example.sistemanutricao.record.PreparacaoDTO.PreparacaoDTO(null, "Teste", 1, "30m", "Fogo", "Modo", null, null, BigDecimal.ONE, BigDecimal.valueOf(10), null);
        
        com.example.sistemanutricao.model.Preparacao prep = new com.example.sistemanutricao.model.Preparacao();
        prep.setRendimento(BigDecimal.valueOf(10));
        when(fichaTecnicaMapper.toPreparacao(prepDto)).thenReturn(prep);

        FichaTecnicaCreateDTO dto = new FichaTecnicaCreateDTO(
            null, null, BigDecimal.TEN, "Prato", null, BigDecimal.valueOf(100), Status.ATIVA, StatusCriacao.COMPLETA, null, null, prepDto, List.of(), null
        );

        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> fichaTecnicaService.create(dto, 1L));
    }

    @Test
    void update_DuplicateName_ThrowsException() {
        FichaTecnica ficha = new FichaTecnica();
        ficha.setId(1L);
        Usuario u = new Usuario(); u.setId(1L);
        ficha.setNutricionista(u);
        when(fichaRepository.findById(1L)).thenReturn(Optional.of(ficha));

        com.example.sistemanutricao.record.PreparacaoDTO.PreparacaoDTO prepDto = 
            new com.example.sistemanutricao.record.PreparacaoDTO.PreparacaoDTO(null, "Arroz", null, "30m", "Fogo", "Modo", null, null, BigDecimal.ONE, BigDecimal.TEN, null);
        
        FichaTecnicaUpdateDTO dto = new FichaTecnicaUpdateDTO(
            null, null, BigDecimal.TEN, "Prato", null, BigDecimal.TEN, Status.ATIVA, StatusCriacao.COMPLETA, prepDto, List.of(), null
        );

        when(fichaRepository.existsByNutricionistaIdAndPreparacaoNomeIgnoreCaseAndIdNot(1L, "Arroz", 1L)).thenReturn(true);

        assertThrows(com.example.sistemanutricao.exception.DuplicateNomeException.class, () -> fichaTecnicaService.update(1L, dto));
    }

    @Test
    void update_DuplicateNumber_ThrowsException() {
        FichaTecnica ficha = new FichaTecnica();
        ficha.setId(1L);
        Usuario u = new Usuario(); u.setId(1L);
        ficha.setNutricionista(u);
        when(fichaRepository.findById(1L)).thenReturn(Optional.of(ficha));

        com.example.sistemanutricao.record.PreparacaoDTO.PreparacaoDTO prepDto = 
            new com.example.sistemanutricao.record.PreparacaoDTO.PreparacaoDTO(null, null, 10, "30m", "Fogo", "Modo", null, null, BigDecimal.ONE, BigDecimal.TEN, null);
        
        FichaTecnicaUpdateDTO dto = new FichaTecnicaUpdateDTO(
            null, null, BigDecimal.TEN, "Prato", null, BigDecimal.TEN, Status.ATIVA, StatusCriacao.COMPLETA, prepDto, List.of(), null
        );

        when(fichaRepository.existsByNutricionistaIdAndPreparacaoNumeroAndIdNot(1L, 10, 1L)).thenReturn(true);

        assertThrows(com.example.sistemanutricao.exception.DuplicateNomeException.class, () -> fichaTecnicaService.update(1L, dto));
    }

    @Test
    void update_NullIngredientes_Success() {
        FichaTecnica ficha = new FichaTecnica();
        ficha.setId(1L);
        Usuario u = new Usuario(); u.setId(1L);
        ficha.setNutricionista(u);
        
        com.example.sistemanutricao.model.Preparacao prepOrig = new com.example.sistemanutricao.model.Preparacao();
        prepOrig.setRendimento(BigDecimal.TEN);
        ficha.setPreparacao(prepOrig);
        
        when(fichaRepository.findById(1L)).thenReturn(Optional.of(ficha));

        com.example.sistemanutricao.record.PreparacaoDTO.PreparacaoDTO prepDto = 
            new com.example.sistemanutricao.record.PreparacaoDTO.PreparacaoDTO(null, "Novo Nome", null, "30m", "Fogo", "Modo", null, null, BigDecimal.TEN, BigDecimal.TEN, null);
        
        com.example.sistemanutricao.model.Preparacao prep = new com.example.sistemanutricao.model.Preparacao();
        prep.setRendimento(BigDecimal.TEN);
        org.mockito.Mockito.lenient().when(fichaTecnicaMapper.toPreparacao(prepDto)).thenReturn(prep);

        FichaTecnicaUpdateDTO dto = new FichaTecnicaUpdateDTO(
            null, null, BigDecimal.TEN, "Prato", null, BigDecimal.TEN, Status.ATIVA, StatusCriacao.COMPLETA, prepDto, null, null
        );

        when(fichaRepository.existsByNutricionistaIdAndPreparacaoNomeIgnoreCaseAndIdNot(1L, "Novo Nome", 1L)).thenReturn(false);

        fichaTecnicaService.update(1L, dto);
        verify(fichaRepository).save(any(FichaTecnica.class));
    }
}
