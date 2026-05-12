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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.example.sistemanutricao.controller.support.PaginacaoViewSupport;
import com.example.sistemanutricao.model.enuns.Cargo;
import com.example.sistemanutricao.record.EstabelecimentoDTO.EstabelecimentoDTO;
import com.example.sistemanutricao.record.EstabelecimentoDTO.GetEstabelecimentoDTO;
import com.example.sistemanutricao.record.UsuarioDTO.GetUsuarioDTO;
import com.example.sistemanutricao.service.EstabelecimentoService;
import com.example.sistemanutricao.service.usuario.UsuarioService;

@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AdminController - Gerenciamento Administrativo")
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private EstabelecimentoService estabelecimentoService;

    @MockitoBean
    private PaginacaoViewSupport paginacaoViewSupport;

    @MockitoBean
    private com.example.sistemanutricao.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private com.example.sistemanutricao.security.CustomAuthenticationProvider authenticationProvider;

    @MockitoBean
    private com.example.sistemanutricao.security.SecurityTokenManager tokenManager;

    @MockitoBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @MockitoBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @MockitoBean
    private com.example.sistemanutricao.security.AuthSessionService authSessionService;


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
}
