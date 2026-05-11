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
import com.example.sistemanutricao.security.UsuarioSecurity;
import com.example.sistemanutricao.service.ficha.FichaQueryService;
import com.example.sistemanutricao.service.ficha.FichaTecnicaService;
import com.example.sistemanutricao.service.port.PdfExporter;

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

    private UsuarioSecurity usuarioNutricionista() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("nutri");
        usuario.setCargo(Cargo.NUTRICIONISTA);
        usuario.setAtivo(true);
        return new UsuarioSecurity(usuario);
    }
}
