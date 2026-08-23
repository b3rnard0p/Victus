package com.example.sistemanutricao.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.security.test.context.support.WithMockUser;

import com.example.sistemanutricao.controller.support.PaginacaoViewSupport;
import com.example.sistemanutricao.model.enuns.Cargo;
import com.example.sistemanutricao.record.EstabelecimentoDTO.EstabelecimentoDTO;
import com.example.sistemanutricao.record.EstabelecimentoDTO.GetEstabelecimentoDTO;
import com.example.sistemanutricao.record.UsuarioDTO.GetUsuarioDTO;
import com.example.sistemanutricao.service.EstabelecimentoService;
import com.example.sistemanutricao.service.usuario.UsuarioService;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminController - Gerenciamento Administrativo")
class AdminControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private EstabelecimentoService estabelecimentoService;

    @Mock
    private PaginacaoViewSupport paginacaoViewSupport;

    @InjectMocks
    private AdminController controller;

    private Long mockedAdminId = 1L;

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
                                u.setId(mockedAdminId);
                                u.setCargo(Cargo.ADMIN);
                                return new com.example.sistemanutricao.security.UsuarioSecurity(u);
                            }
                        },
                        new PageableHandlerMethodArgumentResolver()
                )
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }


    @Test
    @DisplayName("Deve listar usuários com paginação")
    @WithMockUser(roles = "ADMIN")
    void deveListarUsuarios() throws Exception {
        Page<GetUsuarioDTO> page = new PageImpl<>(List.of());
        when(usuarioService.listPage(anyLong(), any(Pageable.class))).thenReturn(page);
        when(paginacaoViewSupport.renderizarView(any(), any(), any(), eq(false)))
                .thenReturn("pages/admin/usuarios/List");

        mockMvc.perform(get("/admin/usuarios"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("usuarios"))
                .andExpect(model().attributeExists("cargos"))
                .andExpect(view().name("pages/admin/usuarios/List"));

        verify(usuarioService).listPage(anyLong(), any(Pageable.class));
    }

    @Test
    @DisplayName("Deve alternar status ativo do usuário")
    @WithMockUser(roles = "ADMIN")
    void deveAlternarAtivo() throws Exception {
        mockMvc.perform(post("/admin/usuarios/1/toggle-ativo"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/admin/usuarios"));

        verify(usuarioService).toggleAtivo(1L);
    }

    @Test
    @DisplayName("Deve atualizar cargo do usuário")
    @WithMockUser(roles = "ADMIN")
    void deveAtualizarCargo() throws Exception {
        mockMvc.perform(post("/admin/usuarios/2/cargo")
                .param("cargo", "NUTRICIONISTA"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/admin/usuarios"));

        verify(usuarioService).updateCargo(2L, Cargo.NUTRICIONISTA);
    }

    @Test
    @DisplayName("Deve alternar status ativo do usuário com HTMX")
    @WithMockUser(roles = "ADMIN")
    void deveAlternarAtivoHtmx() throws Exception {
        when(usuarioService.findById(1L)).thenReturn(new GetUsuarioDTO(1L, "Nome", "email", Cargo.NUTRICIONISTA, null, "Est", true, null));
        mockMvc.perform(post("/admin/usuarios/1/toggle-ativo")
                .header("HX-Request", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/admin/usuarios/List :: usuario-item"));

        verify(usuarioService).toggleAtivo(1L);
    }

    @Test
    @DisplayName("Deve mostrar form de vínculo de estabelecimento")
    @WithMockUser(roles = "ADMIN")
    void deveMostrarFormVinculoEstabelecimento() throws Exception {
        when(usuarioService.findById(1L)).thenReturn(new GetUsuarioDTO(1L, "Nome", "email", Cargo.NUTRICIONISTA, null, "Est", true, null));
        when(estabelecimentoService.listAll()).thenReturn(List.of(new GetEstabelecimentoDTO(1L, "Est", List.of())));
        when(paginacaoViewSupport.renderizarView(any(), any(), any(), eq(false)))
                .thenReturn("pages/admin/usuarios/Form");

        mockMvc.perform(get("/admin/usuarios/1/estabelecimento"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/admin/usuarios/Form"))
                .andExpect(model().attributeExists("estabelecimentoOptions"));
    }

    @Test
    @DisplayName("Deve redirecionar ao tentar vincular em usuário inexistente")
    @WithMockUser(roles = "ADMIN")
    void deveRedirecionarVinculoUsuarioInexistente() throws Exception {
        when(usuarioService.findById(1L)).thenReturn(null);

        mockMvc.perform(get("/admin/usuarios/1/estabelecimento"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/admin/usuarios"));
    }

    @Test
    @DisplayName("Deve atualizar vínculo de estabelecimento")
    @WithMockUser(roles = "ADMIN")
    void deveAtualizarVinculoEstabelecimento() throws Exception {
        mockMvc.perform(post("/admin/usuarios/1/estabelecimento")
                .param("estabelecimentoId", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/admin/usuarios"));

        verify(usuarioService).atualizarEstabelecimento(1L, 2L);
    }

    @Test
    @DisplayName("Deve atualizar vínculo de estabelecimento com HTMX")
    @WithMockUser(roles = "ADMIN")
    void deveAtualizarVinculoEstabelecimentoHtmx() throws Exception {
        when(usuarioService.findById(1L)).thenReturn(new GetUsuarioDTO(1L, "Nome", "email", Cargo.NUTRICIONISTA, null, "Est", true, null));
        mockMvc.perform(post("/admin/usuarios/1/estabelecimento")
                .param("estabelecimentoId", "2")
                .header("HX-Request", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/admin/usuarios/List :: usuario-item"));

        verify(usuarioService).atualizarEstabelecimento(1L, 2L);
    }

    @Test
    @DisplayName("Deve atualizar cargo com HTMX")
    @WithMockUser(roles = "ADMIN")
    void deveAtualizarCargoHtmx() throws Exception {
        when(usuarioService.findById(2L)).thenReturn(new GetUsuarioDTO(2L, "Nome", "email", Cargo.NUTRICIONISTA, null, "Est", true, null));
        mockMvc.perform(post("/admin/usuarios/2/cargo")
                .param("cargo", "NUTRICIONISTA")
                .header("HX-Request", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/admin/usuarios/List :: usuario-item"));

        verify(usuarioService).updateCargo(2L, Cargo.NUTRICIONISTA);
    }

    @Test
    @DisplayName("Não deve permitir atualizar próprio cargo")
    void naoDeveAtualizarProprioCargo() throws Exception {
        mockedAdminId = 2L; // Override ID just for this test so that adminLogado matches the request ID
        setUp(); // Re-setup to recreate mock user with ID 2L

        mockMvc.perform(post("/admin/usuarios/2/cargo")
                .param("cargo", "NUTRICIONISTA"))
                .andExpect(status().isForbidden());
        
        mockedAdminId = 1L; // Reset
    }

    @Test
    @DisplayName("Deve listar estabelecimentos")
    @WithMockUser(roles = "ADMIN")
    void deveListarEstabelecimentos() throws Exception {
        Page<GetEstabelecimentoDTO> page = new PageImpl<>(List.of());
        when(estabelecimentoService.listPage(any(Pageable.class))).thenReturn(page);
        when(paginacaoViewSupport.renderizarView(any(), any(), any(), eq(false)))
                .thenReturn("pages/admin/estabelecimentos/List");

        mockMvc.perform(get("/admin/estabelecimentos"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("estabelecimentos"))
                .andExpect(view().name("pages/admin/estabelecimentos/List"));

        verify(estabelecimentoService).listPage(any(Pageable.class));
    }

    @Test
    @DisplayName("Deve criar novo estabelecimento")
    @WithMockUser(roles = "ADMIN")
    void deveCriarEstabelecimento() throws Exception {
        mockMvc.perform(post("/admin/estabelecimentos")
                .param("nome", "Hospital Central"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/admin/estabelecimentos"));

        verify(estabelecimentoService).create(any(EstabelecimentoDTO.class));
    }

    @Test
    @DisplayName("Deve falhar criar estabelecimento sem nome")
    @WithMockUser(roles = "ADMIN")
    void deveFalharCriarEstabelecimento() throws Exception {
        mockMvc.perform(post("/admin/estabelecimentos")
                .param("nome", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash().attributeExists("errorMessage"));
    }

    @Test
    @DisplayName("Deve mostrar form criar estabelecimento")
    @WithMockUser(roles = "ADMIN")
    void deveMostrarFormCriarEstabelecimento() throws Exception {
        when(paginacaoViewSupport.renderizarView(any(), any(), any(), eq(false)))
                .thenReturn("pages/admin/estabelecimentos/Form");

        mockMvc.perform(get("/admin/estabelecimentos/novo"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/admin/estabelecimentos/Form"));
    }

    @Test
    @DisplayName("Deve mostrar form editar estabelecimento")
    @WithMockUser(roles = "ADMIN")
    void deveMostrarFormEditarEstabelecimento() throws Exception {
        when(estabelecimentoService.findById(1L)).thenReturn(new GetEstabelecimentoDTO(1L, "Nome", List.of()));
        when(paginacaoViewSupport.renderizarView(any(), any(), any(), eq(false)))
                .thenReturn("pages/admin/estabelecimentos/Form");

        mockMvc.perform(get("/admin/estabelecimentos/1/editar"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/admin/estabelecimentos/Form"));
    }

    @Test
    @DisplayName("Deve atualizar estabelecimento")
    @WithMockUser(roles = "ADMIN")
    void deveAtualizarEstabelecimento() throws Exception {
        mockMvc.perform(post("/admin/estabelecimentos/1")
                .param("nome", "Hospital Central"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/admin/estabelecimentos"));

        verify(estabelecimentoService).update(eq(1L), any(EstabelecimentoDTO.class));
    }

    @Test
    @DisplayName("Deve atualizar estabelecimento com HTMX")
    @WithMockUser(roles = "ADMIN")
    void deveAtualizarEstabelecimentoHtmx() throws Exception {
        when(estabelecimentoService.findById(1L)).thenReturn(new GetEstabelecimentoDTO(1L, "Hospital Central", List.of()));
        mockMvc.perform(post("/admin/estabelecimentos/1")
                .param("nome", "Hospital Central")
                .header("HX-Request", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/admin/estabelecimentos/List :: estabelecimento-item"));

        verify(estabelecimentoService).update(eq(1L), any(EstabelecimentoDTO.class));
    }

    @Test
    @DisplayName("Deve falhar ao atualizar estabelecimento com nome em branco")
    @WithMockUser(roles = "ADMIN")
    void deveFalharAtualizarEstabelecimentoBranco() throws Exception {
        mockMvc.perform(post("/admin/estabelecimentos/1")
                .param("nome", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash().attributeExists("errorMessage"));
    }
}
