package com.example.sistemanutricao.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import org.springframework.http.MediaType;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import com.example.sistemanutricao.mapper.FichaFormMapper;
import com.example.sistemanutricao.model.enuns.Cargo;
import com.example.sistemanutricao.model.enuns.Status;
import com.example.sistemanutricao.model.enuns.StatusCriacao;
import com.example.sistemanutricao.model.Usuario;
import com.example.sistemanutricao.record.FichaTecnicaDTO.FichaTecnicaGetDTO;
import com.example.sistemanutricao.record.FichaTecnicaDTO.FichaTecnicaUpdateDTO;
import com.example.sistemanutricao.record.FichaTecnicaDTO.FichaTecnicaCreateDTO;
import com.example.sistemanutricao.record.PreparacaoDTO.PreparacaoDTO;
import com.example.sistemanutricao.record.PreparacaoDTO.PreparacaoGetDTO;
import com.example.sistemanutricao.record.PerfilNutricionalDTO.PerfilNutricionalDTO;
import com.example.sistemanutricao.security.UsuarioSecurity;
import com.example.sistemanutricao.service.ficha.FichaQueryService;
import com.example.sistemanutricao.service.ficha.FichaTecnicaService;
import com.example.sistemanutricao.service.port.PdfExporter;
import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
@DisplayName("FichaController - Unitário com MockMvc")
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

    private UsuarioSecurity usuarioMock;

    @BeforeEach
    void setup() {
        usuarioMock = usuarioNutricionista();
        FichaController controller = new FichaController(fichaTecnicaService, fichaQueryService, pdfExporter, paginacaoViewSupport, fichaFormMapper);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(
                        new HandlerMethodArgumentResolver() {
                            @Override
                            public boolean supportsParameter(MethodParameter parameter) {
                                return parameter.getParameterType().equals(UsuarioSecurity.class);
                            }
                            @Override
                            public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                                        NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                                return usuarioMock;
                            }
                        },
                        new PageableHandlerMethodArgumentResolver()
                )
                .build();
    }

    @Test
    @DisplayName("Deve listar fichas técnicas")
    void deveListarTodasFichas() throws Exception {
        Page<FichaTecnicaGetDTO> page = new PageImpl<>(List.of());
        
        when(fichaQueryService.buscarPorStatus(eq(Status.ATIVA), eq(StatusCriacao.COMPLETA), eq(usuarioMock.getUsuario()), any(Pageable.class)))
                .thenReturn(page);
        when(paginacaoViewSupport.renderizarView(anyString(), any(), any(), anyBoolean()))
                .thenReturn("pages/fichas/List");

        mockMvc.perform(get("/ficha"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("fichas"))
                .andExpect(view().name("pages/fichas/List"));
    }

    @Test
    @DisplayName("Deve pesquisar fichas técnicas")
    void devePesquisarFichas() throws Exception {
        Page<FichaTecnicaGetDTO> page = new PageImpl<>(List.of());
        when(fichaQueryService.pesquisar(eq("nome"), eq("Ficha"), eq("CONTEM"), eq(usuarioMock.getUsuario()), any(Pageable.class)))
                .thenReturn((Page) page);
        when(paginacaoViewSupport.renderizarView(anyString(), any(), any(), anyBoolean()))
                .thenReturn("pages/fichas/List");

        mockMvc.perform(get("/ficha/pesquisar")
                        .param("campo", "nome")
                        .param("valorPesquisa", "Ficha")
                        .param("tipoPesquisa", "CONTEM")
                        .param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/fichas/List"));
    }

    @Test
    @DisplayName("Deve mostrar ficha por ID")
    void deveMostrarFichaPorId() throws Exception {
        FichaTecnicaGetDTO fichaGet = new FichaTecnicaGetDTO(1L, null, null, null, null, null, Status.ATIVA, null, null, null, null);
        when(fichaTecnicaService.getFichaById(1L)).thenReturn(fichaGet);
        when(paginacaoViewSupport.renderizarView(anyString(), any(), any())).thenReturn("pages/fichas/Detail");

        mockMvc.perform(get("/ficha/1"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("ficha"))
                .andExpect(view().name("pages/fichas/Detail"));
    }

    @Test
    @DisplayName("Deve redirecionar ao não encontrar ficha por ID")
    void deveRedirecionarFichaNaoEncontrada() throws Exception {
        when(fichaTecnicaService.getFichaById(1L)).thenThrow(new EntityNotFoundException());

        mockMvc.perform(get("/ficha/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/fichas?error=Ficha não encontrada"));
    }

    @Test
    @DisplayName("Deve alternar status da ficha")
    void deveAlternarStatus() throws Exception {
        mockMvc.perform(post("/ficha/toggle-status/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ficha/1"));
    }

    @Test
    @DisplayName("Deve mostrar formulário de edição")
    void deveMostrarFormularioEdicao() throws Exception {
        FichaTecnicaGetDTO fichaGet = new FichaTecnicaGetDTO(1L, null, null, null, null, null, Status.ATIVA, null, null, null, null);
        FichaTecnicaUpdateDTO fichaEdicao = new FichaTecnicaUpdateDTO(1L, null, null, null, null, null, Status.ATIVA, null, null, null, null);
        
        when(fichaTecnicaService.getFichaById(1L)).thenReturn(fichaGet);
        when(fichaFormMapper.toUpdateDTO(fichaGet)).thenReturn(fichaEdicao);
        when(paginacaoViewSupport.renderizarView(anyString(), any(), any())).thenReturn("pages/fichas/FormFicha");

        mockMvc.perform(get("/ficha/editar/1"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("ficha"))
                .andExpect(view().name("pages/fichas/FormFicha"));
    }

    @Test
    @DisplayName("Deve mostrar formulário para nova ficha")
    void deveMostrarFormularioNovaFicha() throws Exception {
        when(paginacaoViewSupport.renderizarView(anyString(), any(), any())).thenReturn("pages/fichas/FormFicha");

        mockMvc.perform(get("/ficha/nova"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("ficha"))
                .andExpect(view().name("pages/fichas/FormFicha"));
    }

    @Test
    @DisplayName("Deve salvar ficha técnica")
    void deveSalvarFichaTecnica() throws Exception {
        mockMvc.perform(post("/ficha")
                        .param("custoTotal", "10.0")
                        .param("medidaCaseira", "Medida")
                        .param("numeroPorcoes", "1")
                        .param("pesoPorcao", "100.0")
                        .param("preparacao.nome", "Nome")
                        .param("preparacao.tempoPreparo", "10m")
                        .param("preparacao.equipamentos", "Nenhum")
                        .param("preparacao.modoPreparo", "Nenhum")
                        .param("preparacao.fcc", "1.0")
                        .param("preparacao.categoria", "BEBIDA")
                        .param("ingredientes[0].ingredienteId", "1")
                        .param("status", "ATIVA")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ficha"));
    }

    @Test
    @DisplayName("Deve exportar PDF")
    void deveExportarPdf() throws Exception {
        FichaTecnicaGetDTO fichaGet = new FichaTecnicaGetDTO(1L, null, null, null, null, null, Status.ATIVA, null, new PreparacaoGetDTO(1L, "Nome_Ficha", null, null, null, null, null, null, null, null, null), null, null);
        when(fichaTecnicaService.getFichaById(1L)).thenReturn(fichaGet);
        when(pdfExporter.generateFichaTecnicaPdf(fichaGet)).thenReturn(new byte[]{1, 2, 3});

        mockMvc.perform(get("/ficha/exportar-pdf/1"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=NomeFicha.pdf"))
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));
    }

    private UsuarioSecurity usuarioNutricionista() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("nutri");
        usuario.setCargo(Cargo.NUTRICIONISTA);
        usuario.setAtivo(true);
        return new UsuarioSecurity(usuario);
    }
}
