package com.example.sistemanutricao.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.math.BigDecimal;
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
import com.example.sistemanutricao.model.enuns.Cargo;
import com.example.sistemanutricao.model.enuns.Status;
import com.example.sistemanutricao.model.Usuario;
import com.example.sistemanutricao.record.IngredienteDTO.IngredienteGetDTO;
import com.example.sistemanutricao.security.UsuarioSecurity;
import com.example.sistemanutricao.service.ingrediente.IngredienteQueryService;
import com.example.sistemanutricao.service.ingrediente.IngredienteService;

@ExtendWith(MockitoExtension.class)
@DisplayName("IngredienteController - Unitário com MockMvc")
class IngredienteControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IngredienteService ingredienteService;

    @Mock
    private IngredienteQueryService ingredienteQueryService;

    @Mock
    private PaginacaoViewSupport paginacaoViewSupport;

    private UsuarioSecurity usuarioMock;

    @BeforeEach
    void setup() {
        usuarioMock = usuarioNutricionista();
        IngredienteController controller = new IngredienteController(ingredienteService, ingredienteQueryService, paginacaoViewSupport);
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
    @DisplayName("Deve buscar ingredientes padrão")
    void deveBuscarPadrao() throws Exception {
        Page<IngredienteGetDTO> page = new PageImpl<>(List.of(ingredienteDto()));
        
        when(ingredienteQueryService.buscarPorStatusEUsuario(eq(Status.ATIVA), eq(1L), any(Pageable.class)))
                .thenReturn(page);
        when(paginacaoViewSupport.renderizarView(anyString(), any(), any(), anyBoolean()))
                .thenReturn("pages/ingredientes/List");

        mockMvc.perform(get("/ingrediente"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("ingredientes"))
                .andExpect(view().name("pages/ingredientes/List"));

        verify(ingredienteQueryService).buscarPorStatusEUsuario(eq(Status.ATIVA), eq(1L), any(Pageable.class));
    }

    @Test
    @DisplayName("Deve buscar ingredientes por nome")
    void deveBuscarPorNome() throws Exception {
        Page<IngredienteGetDTO> page = new PageImpl<>(List.of(ingredienteDto()));
        
        when((Page) ingredienteQueryService.pesquisar(eq("nome"), eq("arroz"), eq("nome"), eq(1L), any(Pageable.class)))
                .thenReturn(page);

        when(paginacaoViewSupport.renderizarView(anyString(), any(), any(), anyBoolean()))
                .thenReturn("pages/ingredientes/List");

        mockMvc.perform(get("/ingrediente")
                .param("campo", "nome")
                .param("valorPesquisa", "arroz")
                .param("tipoPesquisa", "nome"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/ingredientes/List"));

        verify(ingredienteQueryService).pesquisar(eq("nome"), eq("arroz"), eq("nome"), eq(1L), any(Pageable.class));
    }

    private UsuarioSecurity usuarioNutricionista() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("nutri");
        usuario.setEmail("nutri@example.com");
        usuario.setSenha("senha");
        usuario.setCargo(Cargo.NUTRICIONISTA);
        usuario.setAtivo(true);
        return new UsuarioSecurity(usuario);
    }

    private IngredienteGetDTO ingredienteDto() {
        return new IngredienteGetDTO(
                1L, "Arroz", BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE,
                Status.ATIVA, BigDecimal.ONE, BigDecimal.ONE, 1L
        );
    }
}
