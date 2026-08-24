package com.example.sistemanutricao.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.example.sistemanutricao.controller.support.PaginacaoViewSupport;
import com.example.sistemanutricao.model.enuns.Cargo;
import com.example.sistemanutricao.model.enuns.Status;
import com.example.sistemanutricao.record.RefeicaoDTO.RefeicaoDTO;
import com.example.sistemanutricao.record.RefeicaoDTO.RefeicaoNutrientesResponseDTO;
import com.example.sistemanutricao.record.RefeicaoDTO.RefeicaoResponseDTO;
import com.example.sistemanutricao.service.ficha.FichaTecnicaService;
import com.example.sistemanutricao.service.refeicao.RefeicaoQueryService;
import com.example.sistemanutricao.service.refeicao.RefeicaoService;

@ExtendWith(MockitoExtension.class)
class RefeicaoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RefeicaoService refeicaoService;

    @Mock
    private RefeicaoQueryService refeicaoQueryService;

    @Mock
    private FichaTecnicaService fichaTecnicaService;

    @Mock
    private PaginacaoViewSupport paginacaoViewSupport;

    @InjectMocks
    private RefeicaoController controller;

    private Long mockedNutriId = 1L;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(
                        new HandlerMethodArgumentResolver() {
                            @Override
                            public boolean supportsParameter(MethodParameter parameter) {
                                return parameter.getParameterType().equals(com.example.sistemanutricao.security.UsuarioSecurity.class);
                            }
                            @Override
                            public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                                        NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                                com.example.sistemanutricao.model.Usuario u = new com.example.sistemanutricao.model.Usuario();
                                u.setId(mockedNutriId);
                                u.setCargo(Cargo.NUTRICIONISTA);
                                return new com.example.sistemanutricao.security.UsuarioSecurity(u);
                            }
                        },
                        new PageableHandlerMethodArgumentResolver()
                )
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Deve retornar totais de nutrientes")
    void deveRetornarTotaisNutrientes() throws Exception {
        RefeicaoNutrientesResponseDTO dto = new RefeicaoNutrientesResponseDTO(1L, "Nome", "100", Status.ATIVA, List.of(), java.math.BigDecimal.TEN, java.math.BigDecimal.TEN, java.math.BigDecimal.TEN, java.math.BigDecimal.TEN, java.math.BigDecimal.TEN);
        when(refeicaoService.buscarTotaisNutrientesPorId(1L)).thenReturn(dto);

        mockMvc.perform(get("/refeicao/1/nutrientes-totais"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve buscar com paginacao e renderizar view")
    void deveBuscarComPaginacaoERenderizarView() throws Exception {
        Page<RefeicaoResponseDTO> page = new PageImpl<>(List.of());
        when(refeicaoQueryService.buscarPorStatus(eq(Status.ATIVA), any(), any())).thenReturn(page);
        when(fichaTecnicaService.listarResumo()).thenReturn(List.of());
        when(paginacaoViewSupport.renderizarView(any(), any(), any(), eq(false)))
                .thenReturn("pages/refeicoes/List");

        mockMvc.perform(get("/refeicao"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/refeicoes/List"))
                .andExpect(model().attributeExists("refeicoes", "statusAtual", "fichasTecnicasLista"));
    }
    
    @Test
    @DisplayName("Deve pesquisar por nome via get")
    void devePesquisarPorNome() throws Exception {
        Page<RefeicaoResponseDTO> page = new PageImpl<>(List.of());
        when(refeicaoQueryService.buscarPorNome(eq("Café"), any(), any())).thenReturn(page);
        when(fichaTecnicaService.listarResumo()).thenReturn(List.of());
        when(paginacaoViewSupport.renderizarView(any(), any(), any(), eq(false)))
                .thenReturn("pages/refeicoes/List");

        mockMvc.perform(get("/refeicao/pesquisar").param("nome", "Café"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("termoBusca"));
    }

    @Test
    @DisplayName("Deve fazer toggle status")
    void deveFazerToggleStatus() throws Exception {
        mockMvc.perform(post("/refeicao/toggle-status/1").param("currentStatus", "ATIVA"))
                .andExpect(status().isFound())
                .andExpect(view().name("redirect:/refeicao"));
                
        verify(refeicaoService).atualizaStatus(1L);
    }
    
    @Test
    @DisplayName("Deve fazer toggle status redirecionando para inativas")
    void deveFazerToggleStatusInativas() throws Exception {
        mockMvc.perform(post("/refeicao/toggle-status/1").param("currentStatus", "INATIVA"))
                .andExpect(status().isFound())
                .andExpect(view().name("redirect:/refeicao/por-status?status=INATIVA"));
                
        verify(refeicaoService).atualizaStatus(1L);
    }

    @Test
    @DisplayName("Deve mostrar form edicao retornando dto json")
    void deveMostrarFormEdicao() throws Exception {
        RefeicaoResponseDTO dto = new RefeicaoResponseDTO(1L, "Café", "10", Status.ATIVA, List.of());
        when(refeicaoService.buscarPorId(1L)).thenReturn(dto);

        mockMvc.perform(get("/refeicao/editar/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve criar refeicao")
    void deveCriarRefeicao() throws Exception {
        mockMvc.perform(post("/refeicao/novo")
                .param("nome", "Almoço")
                .param("fichasTecnicasIds", "1", "2"))
                .andExpect(status().isFound())
                .andExpect(view().name("redirect:/refeicao"));

        verify(refeicaoService).create(any(RefeicaoDTO.class), eq(1L));
    }
    
    @Test
    @DisplayName("Deve dar erro ao criar refeicao com nome em branco")
    void deveDarErroAoCriarRefeicao() throws Exception {
        mockMvc.perform(post("/refeicao/novo")
                .param("nome", ""))
                .andExpect(status().isFound()); // Redireciona via exception handler

        verify(refeicaoService, org.mockito.Mockito.never()).create(any(), any());
    }

    @Test
    @DisplayName("Deve atualizar refeicao normalmente")
    void deveAtualizarRefeicaoNormalmente() throws Exception {
        mockMvc.perform(post("/refeicao/editar/1")
                .param("nome", "Janta")
                .param("fichasTecnicasIds", "1", "2"))
                .andExpect(status().isFound())
                .andExpect(view().name("redirect:/refeicao"));

        verify(refeicaoService).update(eq(1L), any(RefeicaoDTO.class));
    }

    @Test
    @DisplayName("Deve atualizar refeicao via htmx")
    void deveAtualizarRefeicaoHtmx() throws Exception {
        RefeicaoResponseDTO dto = new RefeicaoResponseDTO(1L, "Janta", "10", Status.ATIVA, List.of());
        when(refeicaoService.buscarPorId(1L)).thenReturn(dto);

        mockMvc.perform(post("/refeicao/editar/1")
                .param("nome", "Janta")
                .param("fichasTecnicasIds", "1", "2")
                .header("HX-Request", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/refeicoes/List :: refeicao-item"));

        verify(refeicaoService).update(eq(1L), any(RefeicaoDTO.class));
    }
}
