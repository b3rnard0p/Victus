package com.example.sistemanutricao.service.ficha;

import com.example.sistemanutricao.exception.FichaTecnicaNotFoundException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.sistemanutricao.model.FichaTecnica;
import com.example.sistemanutricao.model.PerfilNutricional;
import com.example.sistemanutricao.model.Preparacao;
import com.example.sistemanutricao.model.enuns.Status;
import com.example.sistemanutricao.model.enuns.StatusCriacao;
import com.example.sistemanutricao.model.Usuario;
import com.example.sistemanutricao.record.FichaTecnicaDTO.FichaTecnicaGetDTO;
import com.example.sistemanutricao.record.FichaTecnicaDTO.FichaTecnicaCreateDTO;
import com.example.sistemanutricao.record.PreparacaoDTO.PreparacaoDTO;
import com.example.sistemanutricao.mapper.FichaTecnicaMapper;
import com.example.sistemanutricao.repository.FichaTecnicaRepository;
import com.example.sistemanutricao.repository.IngredienteRepository;
import com.example.sistemanutricao.repository.UsuarioRepository;
import org.mapstruct.factory.Mappers;

@ExtendWith(MockitoExtension.class)
class FichaTecnicaServiceTest {

    @Mock
    private FichaTecnicaRepository fichaRepository;

    @Mock
    private IngredienteRepository ingredienteRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    private FichaTecnicaService fichaTecnicaService;
    private FichaTecnicaMapper fichaTecnicaMapper;

    @BeforeEach
    void setUp() {
        fichaTecnicaMapper = Mappers.getMapper(FichaTecnicaMapper.class);
        fichaTecnicaService = new FichaTecnicaService(
            fichaRepository,
            ingredienteRepository,
            usuarioRepository,
            new PerfilNutricionalCalculator(ingredienteRepository),
            fichaTecnicaMapper
        );
    }

    @Test
    void shouldReturnFichaTecnicaDtoWhenFoundById() {
        FichaTecnica ficha = new FichaTecnica();
        ficha.setId(12L);
        ficha.setCustoPerCapita(new BigDecimal("12.50"));
        ficha.setCustoTotal(new BigDecimal("100.00"));
        ficha.setMedidaCaseira("porção");
        ficha.setNumeroPorcoes(8);
        ficha.setPesoPorcao(new BigDecimal("150.00"));
        ficha.setStatus(Status.ATIVA);
        ficha.setStatusCriacao(StatusCriacao.COMPLETA);
        
        Preparacao prep = new Preparacao();
        prep.setId(20L);
        prep.setNome("Arroz");
        ficha.setPreparacao(prep);
        
        PerfilNutricional perfil = new PerfilNutricional();
        perfil.setId(30L);
        perfil.setVtc(new BigDecimal("320.00"));
        ficha.setPerfilNutricional(perfil);
        
        ficha.setIngredientesPorFicha(new ArrayList<>());

        when(fichaRepository.findById(12L)).thenReturn(Optional.of(ficha));

        FichaTecnicaGetDTO dto = fichaTecnicaService.getFichaById(12L);

        assertThat(dto.id()).isEqualTo(12L);
        assertThat(dto.custoPerCapita()).isEqualByComparingTo("12.50");
        assertThat(dto.preparacao().id()).isEqualTo(20L);
        assertThat(dto.perfilNutricional().id()).isEqualTo(30L);
    }

    @Test
    void shouldThrowFichaTecnicaNotFoundExceptionWhenMissing() {
        when(fichaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> fichaTecnicaService.getFichaById(99L))
                .isInstanceOf(FichaTecnicaNotFoundException.class)
                .hasMessage("Ficha técnica não encontrada com ID: 99");
    }

    @Test
    void shouldCreateFichaAndCalculateNumeroPorcoesAndCustoPerCapita() {
        when(fichaRepository.save(any())).thenAnswer(invocation -> {
            FichaTecnica f = invocation.getArgument(0);
            f.setId(12L);
            if (f.getPreparacao() != null) f.getPreparacao().setId(20L);
            if (f.getPerfilNutricional() != null) f.getPerfilNutricional().setId(30L);
            return f;
        });

        PreparacaoDTO preparacaoDto = new PreparacaoDTO(
                null, "Arroz", 1, "30 min", "Panela", "Cozinhar",
                null, null, new BigDecimal("0.00"), new BigDecimal("800.00"), null
        );

        FichaTecnicaCreateDTO fichaCreate = new FichaTecnicaCreateDTO(
                null, null, new BigDecimal("100.00"), "porção", null, new BigDecimal("150.00"),
                Status.ATIVA, StatusCriacao.COMPLETA, null, null, preparacaoDto, new ArrayList<>(), null
        );

        Usuario nutricionista = new Usuario();
        nutricionista.setId(1L);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(nutricionista));

        FichaTecnicaGetDTO dto = fichaTecnicaService.create(fichaCreate, 1L);

        // rendimento 800 / pesoPorcao 150 -> 5 porções
        assertThat(dto.numeroPorcoes()).isEqualTo(5);
        // 100 / 5 = 20.00
        assertThat(dto.custoPerCapita()).isEqualByComparingTo("20.00");
    }
}
