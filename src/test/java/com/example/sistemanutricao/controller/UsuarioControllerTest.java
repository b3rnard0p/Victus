package com.example.sistemanutricao.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import com.example.sistemanutricao.model.enuns.Cargo;
import com.example.sistemanutricao.model.Usuario;
import com.example.sistemanutricao.record.UsuarioDTO.GetUsuarioDTO;
import com.example.sistemanutricao.security.AuthSessionService;
import com.example.sistemanutricao.security.LoginStatus;
import com.example.sistemanutricao.security.UsuarioSecurity;
import com.example.sistemanutricao.service.usuario.PasswordValidationService;
import com.example.sistemanutricao.service.usuario.UsuarioService;

@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioController - Unitário com MockMvc")
class UsuarioControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private AuthSessionService authSessionService;

    @Mock
    private PasswordValidationService passwordValidationService;

    private UsuarioSecurity usuarioMock;

    @BeforeEach
    void setup() {
        usuarioMock = usuarioNutricionista();
        UsuarioController controller = new UsuarioController(usuarioService, authSessionService, passwordValidationService);
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
                        }
                )
                .build();
    }

    @Test
    @DisplayName("Deve exibir home")
    void deveExibirHome() throws Exception {
        mockMvc.perform(get("/home"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/general/Home"));
    }

    @Test
    void deveExibirLogin() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/general/Login"));
    }

    @Test
    void deveExibirAcessoNegado() throws Exception {
        mockMvc.perform(get("/acesso-negado"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/general/acesso-negado"));
    }

    @Test
    void deveExibirRegistroSucesso() throws Exception {
        mockMvc.perform(get("/registro-sucesso"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/general/Registrado"));
    }

    @Test
    void deveRealizarLoginComSucesso() throws Exception {
        when(authSessionService.login(anyString(), anyString(), anyBoolean(), any())).thenReturn(LoginStatus.SUCCESS);
        mockMvc.perform(post("/login").param("email", "a").param("password", "b"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/home"));
    }

    @Test
    void deveRealizarLoginComAcessoNegado() throws Exception {
        when(authSessionService.login(anyString(), anyString(), anyBoolean(), any())).thenReturn(LoginStatus.ACCESS_DENIED);
        mockMvc.perform(post("/login").param("email", "a").param("password", "b"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/acesso-negado"));
    }

    @Test
    void deveRealizarLoginComErro() throws Exception {
        when(authSessionService.login(anyString(), anyString(), anyBoolean(), any())).thenReturn(LoginStatus.INVALID_CREDENTIALS);
        mockMvc.perform(post("/login").param("email", "a").param("password", "b"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"));
    }

    @Test
    void deveRealizarLogout() throws Exception {
        mockMvc.perform(post("/sair-do-sistema"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"));
    }

    @Test
    void deveVerificarStatusAutenticacao() throws Exception {
        mockMvc.perform(get("/auth/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.autenticado").value(true));
    }

    @Test
    void deveRedirecionarPerfil() throws Exception {
        mockMvc.perform(get("/usuario/perfil"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/home"));
    }

    @Test
    @DisplayName("Deve exibir modal de perfil")
    void deveExibirPerfilModal() throws Exception {
        GetUsuarioDTO dto = new GetUsuarioDTO(1L, "nutri", "nutri@example.com", Cargo.NUTRICIONISTA, null, "", true, "");
        when(usuarioService.findById(anyLong())).thenReturn(dto);

        mockMvc.perform(get("/usuario/perfil/modal"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/Perfil"));

        verify(usuarioService).findById(eq(1L));
    }

    @Test
    @DisplayName("Deve registrar usuario e redirecionar para sucesso")
    void deveRegistrarUsuario() throws Exception {
        when(usuarioService.create(org.mockito.ArgumentMatchers.any())).thenReturn(new Usuario());

        mockMvc.perform(post("/usuario/registrar")
                        .param("username", "novo.usuario")
                        .param("email", "novo.usuario@example.com")
                        .param("senha", "Senha123!")
                        .param("confirmarSenha", "Senha123!")
                        .param("cargo", "NUTRICIONISTA"))
            .andExpect(status().isOk())
            .andExpect(view().name("pages/general/Registrado"));

        verify(usuarioService).create(org.mockito.ArgumentMatchers.any());
    }
    
    @Test
    void deveEditarPerfil() throws Exception {
        when(passwordValidationService.validar(any(), any(), any(), any())).thenReturn(java.util.Optional.empty());
        mockMvc.perform(post("/usuario/editar")
                        .param("username", "novo.usuario")
                        .param("email", "novo.usuario@example.com")
                        .param("senha", "Senha123!")
                        .param("confirmarSenha", "Senha123!")
                        .param("cargo", "NUTRICIONISTA"))
            .andExpect(status().is3xxRedirection());
    }
    
    @Test
    void deveObterImagem() throws Exception {
        Resource r = new ByteArrayResource("img".getBytes(), "img.jpg");
        when(usuarioService.obterImagemPerfil(1L)).thenReturn(r);
        mockMvc.perform(get("/usuario/1/imagem"))
            .andExpect(status().isOk());
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
