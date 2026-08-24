package com.example.sistemanutricao.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;
import java.util.ArrayList;

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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.example.sistemanutricao.controller.support.PaginacaoViewSupport;
import com.example.sistemanutricao.mapper.FichaFormMapper;
import com.example.sistemanutricao.model.enuns.Cargo;
import com.example.sistemanutricao.model.enuns.Status;
import com.example.sistemanutricao.model.enuns.StatusCriacao;
import com.example.sistemanutricao.record.FichaTecnicaDTO.FichaTecnicaCreateDTO;
import com.example.sistemanutricao.record.FichaTecnicaDTO.FichaTecnicaGetDTO;
import com.example.sistemanutricao.record.FichaTecnicaDTO.FichaTecnicaUpdateDTO;
import com.example.sistemanutricao.record.PerfilNutricionalDTO.PerfilNutricionalDTO;
import com.example.sistemanutricao.record.PreparacaoDTO.PreparacaoDTO;
import com.example.sistemanutricao.record.PreparacaoDTO.PreparacaoGetDTO;
import com.example.sistemanutricao.security.UsuarioSecurity;
import com.example.sistemanutricao.service.ficha.FichaQueryService;
import com.example.sistemanutricao.service.ficha.FichaTecnicaService;
import com.example.sistemanutricao.service.port.PdfExporter;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class FichaControllerTest {

    private MockMvc mockMvc;

    @Mock
    private FichaTecnicaService fichaTecnicaService;

    @Mock
    private FichaQueryService fichaQueryService;

    @Mock
    private PdfExporter pdfExporter;

    @Mock
    private FichaFormMapper fichaFormMapper;

    @Mock
    private PaginacaoViewSupport paginacaoViewSupport;

    @InjectMocks
    private FichaController controller;

    private Long mockedUserId = 1L;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new HandlerMethodArgumentResolver() {
                    @Override
                    public boolean supportsParameter(MethodParameter parameter) {
                        return parameter.getParameterType().equals(UsuarioSecurity.class);
                    }
                    @Override
                    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                        com.example.sistemanutricao.model.Usuario u = new com.example.sistemanutricao.model.Usuario();
                        u.setId(mockedUserId);
                        u.setCargo(Cargo.NUTRICIONISTA);
                        return new UsuarioSecurity(u);
                    }
                }, new PageableHandlerMethodArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Deve redirecionar buscas antigas")
    void deveRedirecionarBuscasAntigas() throws Exception {
        mockMvc.perform(get("/ficha/por-nome?query=test"))
                .andExpect(status().isFound())
                .andExpect(view().name("redirect:/ficha?query=test"));
    }

    @Test
    @DisplayName("Deve listar todas as fichas")
    void deveListarTodasFichas() throws Exception {
        Page<FichaTecnicaGetDTO> page = new PageImpl<>(List.of());
        when(fichaQueryService.buscarPorStatus(eq(Status.ATIVA), eq(StatusCriacao.COMPLETA), any(), any())).thenReturn(page);
        when(paginacaoViewSupport.renderizarView(any(), any(), any(), eq(false))).thenReturn("pages/fichas/List");

        mockMvc.perform(get("/ficha"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/fichas/List"))
                .andExpect(model().attributeExists("fichas", "statusAtual"));
    }

    @Test
    @DisplayName("Deve buscar fichas por campo e valor")
    void deveBuscarFichasPorCampoEValor() throws Exception {
        @SuppressWarnings("unchecked")
        Page<FichaTecnicaGetDTO> page = new PageImpl<>(List.of());
        when(fichaQueryService.pesquisar(eq("nome"), eq("Arroz"), eq("texto"), any(), any())).thenAnswer(inv -> page);
        when(paginacaoViewSupport.renderizarView(any(), any(), any(), eq(false))).thenReturn("pages/fichas/List");

        mockMvc.perform(get("/ficha/buscar")
                .param("campo", "nome")
                .param("valorPesquisa", "Arroz")
                .param("tipoPesquisa", "texto"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/fichas/List"));
    }

    @Test
    @DisplayName("Deve pesquisar fichas redirecionando para buscar")
    void devePesquisarFichas() throws Exception {
        @SuppressWarnings("unchecked")
        Page<FichaTecnicaGetDTO> page = new PageImpl<>(List.of());
        when(fichaQueryService.pesquisar(eq("nome"), eq("Arroz"), eq("texto"), any(), any())).thenAnswer(inv -> page);
        when(paginacaoViewSupport.renderizarView(any(), any(), any(), eq(false))).thenReturn("pages/fichas/List");

        mockMvc.perform(get("/ficha/pesquisar")
                .param("campo", "nome")
                .param("valorPesquisa", "Arroz")
                .param("tipoPesquisa", "texto"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/fichas/List"));
    }

    @Test
    @DisplayName("Deve buscar fichas por tags")
    void deveBuscarFichasPorTags() throws Exception {
        @SuppressWarnings("unchecked")
        Page<FichaTecnicaGetDTO> pageMock = new PageImpl<>(List.of());
        when(fichaQueryService.pesquisar(eq("tag"), eq("Alta"), eq("tags"), any(), any()))
                .thenAnswer(i -> pageMock);

        when(paginacaoViewSupport.renderizarView(any(), any(), any(), eq(false))).thenReturn("pages/fichas/List");

        mockMvc.perform(get("/ficha/buscar")
                .param("campo", "tag")
                .param("valorPesquisa", "Alta")
                .param("tipoPesquisa", "tags"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/fichas/List"));
    }

    @Test
    @DisplayName("Deve buscar fichas paginadas")
    void deveBuscarFichasPaginadas() throws Exception {
        @SuppressWarnings("unchecked")
        Page<FichaTecnicaGetDTO> pageMock = new PageImpl<>(List.of());
        when(fichaQueryService.buscarPorStatus(eq(Status.ATIVA), eq(StatusCriacao.COMPLETA), any(), any()))
                .thenAnswer(i -> pageMock);

        when(paginacaoViewSupport.renderizarView(any(), any(), any(), eq(true))).thenReturn("pages/fichas/List");

        mockMvc.perform(get("/ficha/buscar")
                .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/fichas/List"));
    }

    @Test
    @DisplayName("Deve buscar fichas por status de criacao")
    void deveBuscarFichasPorStatusCriacao() throws Exception {
        @SuppressWarnings("unchecked")
        Page<FichaTecnicaGetDTO> pageMock = new PageImpl<>(List.of());
        when(fichaQueryService.buscarPorStatus(eq(Status.ATIVA), eq(StatusCriacao.INCOMPLETA), any(), any()))
                .thenAnswer(i -> pageMock);

        when(paginacaoViewSupport.renderizarView(any(), any(), any(), eq(false))).thenReturn("pages/fichas/List");

        mockMvc.perform(get("/ficha/buscar")
                .param("statusCriacao", "INCOMPLETA"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/fichas/List"));
    }

    @Test
    @DisplayName("Deve mostrar ficha por id")
    void deveMostrarFichaPorId() throws Exception {
        FichaTecnicaGetDTO ficha = new FichaTecnicaGetDTO(1L, null, null, null, null, null, Status.ATIVA, StatusCriacao.COMPLETA, null, null, null);
        when(fichaTecnicaService.getFichaById(1L)).thenReturn(ficha);
        when(paginacaoViewSupport.renderizarView(any(), any(), any())).thenReturn("pages/fichas/Detail");

        mockMvc.perform(get("/ficha/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/fichas/Detail"));
    }

    @Test
    @DisplayName("Deve retornar not found ao mostrar ficha por id inexistente")
    void deveRetornarNotFoundAoMostrarFicha() throws Exception {
        when(fichaTecnicaService.getFichaById(1L)).thenThrow(new EntityNotFoundException());

        mockMvc.perform(get("/ficha/1"))
                .andExpect(status().isFound())
                .andExpect(view().name("redirect:/fichas?error=Ficha não encontrada"));
    }

    @Test
    @DisplayName("Deve fazer toggle status")
    void deveToggleStatus() throws Exception {
        mockMvc.perform(post("/ficha/toggle-status/1"))
                .andExpect(status().isFound())
                .andExpect(view().name("redirect:/ficha/1"));
        verify(fichaTecnicaService).atualizaStatus(1L);
    }

    @Test
    @DisplayName("Deve mostrar formulario edicao")
    void deveMostrarFormularioEdicao() throws Exception {
        FichaTecnicaGetDTO fichaGet = new FichaTecnicaGetDTO(1L, null, null, null, null, null, Status.ATIVA, StatusCriacao.COMPLETA, null, null, null);
        FichaTecnicaUpdateDTO fichaUpdate = new FichaTecnicaUpdateDTO(1L, null, null, null, null, null, Status.ATIVA, StatusCriacao.COMPLETA, null, null, null);
        
        when(fichaTecnicaService.getFichaById(1L)).thenReturn(fichaGet);
        when(fichaFormMapper.toUpdateDTO(fichaGet)).thenReturn(fichaUpdate);
        when(paginacaoViewSupport.renderizarView(any(), any(), any())).thenReturn("pages/fichas/FormFicha");

        mockMvc.perform(get("/ficha/editar/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/fichas/FormFicha"))
                .andExpect(model().attributeExists("ficha", "categorias"));
    }

    @Test
    @DisplayName("Deve mostrar formulario nova ficha")
    void deveMostrarFormularioNovaFicha() throws Exception {
        when(paginacaoViewSupport.renderizarView(any(), any(), any())).thenReturn("pages/fichas/FormFicha");

        mockMvc.perform(get("/ficha/nova"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/fichas/FormFicha"))
                .andExpect(model().attributeExists("ficha", "categorias"));
    }

    @Test
    @DisplayName("Deve exportar pdf")
    void deveExportarPdf() throws Exception {
        PreparacaoGetDTO prep = new PreparacaoGetDTO(1L, "Prep", null, null, null, null, null, null, null, null, null);
        FichaTecnicaGetDTO fichaGet = new FichaTecnicaGetDTO(1L, null, null, null, null, null, Status.ATIVA, StatusCriacao.COMPLETA, prep, null, null);
        when(fichaTecnicaService.getFichaById(1L)).thenReturn(fichaGet);
        when(pdfExporter.generateFichaTecnicaPdf(fichaGet)).thenReturn(new byte[]{1,2,3});

        mockMvc.perform(get("/ficha/exportar-pdf/1"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Prep.pdf"));
    }

    @Test
    @DisplayName("Deve retornar erro ao exportar pdf inexistente")
    void deveRetornarErroExportarPdf() throws Exception {
        when(fichaTecnicaService.getFichaById(1L)).thenThrow(new EntityNotFoundException());

        mockMvc.perform(get("/ficha/exportar-pdf/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve atualizar ficha tecnica")
    void deveAtualizarFichaTecnica() throws Exception {
        mockMvc.perform(post("/ficha/editar/1")
                .param("custoTotal", "100.00")
                .param("medidaCaseira", "Colher")
                .param("numeroPorcoes", "10")
                .param("pesoPorcao", "50.0")
                .param("statusCriacao", "COMPLETA")
                .param("ingredientes[0].ingredienteId", "1")
                .param("ingredientes[0].pesoBruto", "100.0"))
                .andExpect(status().isFound())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl("/ficha"));
        verify(fichaTecnicaService).update(eq(1L), any());
    }

    @Test
    @DisplayName("Deve atualizar ficha tecnica com status incompleta")
    void deveAtualizarFichaTecnicaIncompleta() throws Exception {
        mockMvc.perform(post("/ficha/editar/1")
                .param("custoTotal", "100.00")
                .param("medidaCaseira", "Colher")
                .param("numeroPorcoes", "10")
                .param("pesoPorcao", "50.0")
                .param("statusCriacao", "INCOMPLETA")
                .param("ingredientes[0].ingredienteId", "1")
                .param("ingredientes[0].pesoBruto", "100.0"))
                .andExpect(status().isFound())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl("/ficha/por-statusCriacao?statusCriacao=INCOMPLETA"));
    }

    @Test
    @DisplayName("Deve atualizar ficha tecnica com htmx")
    void deveAtualizarFichaTecnicaHtmx() throws Exception {
        FichaTecnicaGetDTO ficha = new FichaTecnicaGetDTO(1L, null, null, null, null, null, Status.ATIVA, StatusCriacao.COMPLETA, null, null, null);
        when(fichaTecnicaService.getFichaById(1L)).thenReturn(ficha);

        mockMvc.perform(post("/ficha/editar/1")
                .header("HX-Request", "true")
                .param("custoTotal", "100.00")
                .param("medidaCaseira", "Colher")
                .param("numeroPorcoes", "10")
                .param("pesoPorcao", "50.0")
                .param("statusCriacao", "COMPLETA")
                .param("ingredientes[0].ingredienteId", "1")
                .param("ingredientes[0].pesoBruto", "100.0"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/fichas/List :: ficha-item"));
    }

    @Test
    @DisplayName("Deve atualizar ficha tecnica retornando htmx reswap")
    void deveAtualizarFichaTecnicaHtmxError() throws Exception {
        mockMvc.perform(post("/ficha/editar/1")
                .header("HX-Request", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("components/Toast :: error"));
    }

    @Test
    @DisplayName("Deve falhar validacao ao atualizar ficha tecnica normal")
    void deveFalharValidacaoAoAtualizarFichaTecnica() throws Exception {
        mockMvc.perform(post("/ficha/editar/1"))
                .andExpect(status().isFound())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl("/"));
    }

    @Test
    @DisplayName("Deve salvar ficha tecnica")
    void deveSalvarFichaTecnica() throws Exception {
        mockMvc.perform(post("/ficha")
                .param("custoTotal", "100.00")
                .param("medidaCaseira", "Colher")
                .param("numeroPorcoes", "10")
                .param("pesoPorcao", "50.0")
                .param("ingredientes[0].ingredienteId", "1")
                .param("ingredientes[0].pesoBruto", "10.0"))
                .andExpect(status().isFound())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl("/ficha"));
    }

    @Test
    @DisplayName("Deve salvar ficha tecnica com htmx")
    void deveSalvarFichaTecnicaHtmx() throws Exception {
        mockMvc.perform(post("/ficha")
                .header("HX-Request", "true")
                .param("custoTotal", "100.00")
                .param("medidaCaseira", "Colher")
                .param("numeroPorcoes", "10")
                .param("pesoPorcao", "50.0")
                .param("ingredientes[0].ingredienteId", "1")
                .param("ingredientes[0].pesoBruto", "10.0"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header().string("HX-Redirect", "/ficha"));
    }

    @Test
    @DisplayName("Deve falhar validacao ao salvar ficha tecnica")
    void deveFalharValidacaoSalvarFichaTecnica() throws Exception {
        mockMvc.perform(post("/ficha"))
                .andExpect(status().isFound())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl("/"));
    }
}
