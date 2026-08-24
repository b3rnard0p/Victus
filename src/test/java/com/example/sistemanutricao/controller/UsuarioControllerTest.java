package com.example.sistemanutricao.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.example.sistemanutricao.model.enuns.Cargo;
import com.example.sistemanutricao.record.UsuarioDTO.GetUsuarioDTO;
import com.example.sistemanutricao.record.UsuarioDTO.UsuarioDTO;
import com.example.sistemanutricao.security.AuthSessionService;
import com.example.sistemanutricao.security.LoginStatus;
import com.example.sistemanutricao.security.UsuarioSecurity;
import com.example.sistemanutricao.service.usuario.PasswordValidationService;
import com.example.sistemanutricao.service.usuario.UsuarioService;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private AuthSessionService authSessionService;

    @Mock
    private PasswordValidationService passwordValidationService;

    @InjectMocks
    private UsuarioController controller;

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
                        u.setUsername("Teste");
                        return new UsuarioSecurity(u);
                    }
                })
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Deve retornar view home")
    void deveRetornarHome() throws Exception {
        mockMvc.perform(get("/home"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/general/Home"));
    }

    @Test
    @DisplayName("Deve retornar view login")
    void deveRetornarLogin() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/general/Login"));
    }

    @Test
    @DisplayName("Deve realizar login com sucesso")
    void deveRealizarLoginSucesso() throws Exception {
        when(authSessionService.login(anyString(), anyString(), anyBoolean(), any())).thenReturn(LoginStatus.SUCCESS);
        mockMvc.perform(post("/login").param("email", "teste@teste.com").param("password", "123"))
                .andExpect(status().isFound())
                .andExpect(view().name("redirect:/home"));
    }

    @Test
    @DisplayName("Deve realizar login com acesso negado")
    void deveRealizarLoginAcessoNegado() throws Exception {
        when(authSessionService.login(anyString(), anyString(), anyBoolean(), any())).thenReturn(LoginStatus.ACCESS_DENIED);
        mockMvc.perform(post("/login").param("email", "teste@teste.com").param("password", "123"))
                .andExpect(status().isFound())
                .andExpect(view().name("redirect:/acesso-negado"));
    }

    @Test
    @DisplayName("Deve realizar login com falha")
    void deveRealizarLoginFalha() throws Exception {
        when(authSessionService.login(anyString(), anyString(), anyBoolean(), any())).thenReturn(LoginStatus.INVALID_CREDENTIALS);
        mockMvc.perform(post("/login").param("email", "teste@teste.com").param("password", "123"))
                .andExpect(status().isFound())
                .andExpect(view().name("redirect:/"));
    }

    @Test
    @DisplayName("Deve realizar logout")
    void deveRealizarLogout() throws Exception {
        mockMvc.perform(post("/sair-do-sistema"))
                .andExpect(status().isFound())
                .andExpect(view().name("redirect:/"));
        verify(authSessionService).logout(any());
    }

    @Test
    @DisplayName("Deve registrar usuario")
    void deveRegistrarUsuario() throws Exception {
        mockMvc.perform(post("/usuario/registrar")
                .param("username", "teste")
                .param("email", "teste@teste.com")
                .param("password", "123"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/general/Registrado"));
        verify(usuarioService).create(any(UsuarioDTO.class));
    }
    
    @Test
    @DisplayName("Deve falhar ao registrar usuario com dados invalidos")
    void deveFalharRegistrarUsuario() throws Exception {
        mockMvc.perform(post("/usuario/registrar")
                .param("username", "")
                .param("email", "teste@teste.com")
                .param("password", "123"))
                .andExpect(status().isFound()); // Exception handler intercepta
    }

    @Test
    @DisplayName("Deve mostrar sucesso no registro")
    void deveMostrarSucessoRegistro() throws Exception {
        mockMvc.perform(get("/registro-sucesso"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/general/Registrado"));
    }
    
    @Test
    @DisplayName("Deve redirecionar do perfil")
    void deveRedirecionarPerfil() throws Exception {
        mockMvc.perform(get("/usuario/perfil"))
                .andExpect(status().isFound())
                .andExpect(view().name("redirect:/home"));
    }

    @Test
    @DisplayName("Deve mostrar modal de perfil")
    void deveMostrarModalPerfil() throws Exception {
        GetUsuarioDTO dto = new GetUsuarioDTO(1L, "Teste", "teste@teste.com", Cargo.NUTRICIONISTA, null, null, true, null);
        when(usuarioService.findById(1L)).thenReturn(dto);

        mockMvc.perform(get("/usuario/perfil/modal"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/Perfil"))
                .andExpect(model().attributeExists("usuario"));
    }

    @Test
    @DisplayName("Deve editar perfil com sucesso via form normal")
    void deveEditarPerfilNormal() throws Exception {
        when(passwordValidationService.validar(any(), any(), any(), any())).thenReturn(Optional.empty());

        mockMvc.perform(multipart("/usuario/editar")
                .param("username", "teste2")
                .param("email", "teste@teste.com")
                .param("password", "123"))
                .andExpect(status().isFound())
                .andExpect(view().name("redirect:/home"));

        verify(usuarioService).atualizarPerfilComImagem(eq(1L), any(UsuarioDTO.class), any());
    }
    
    @Test
    @DisplayName("Deve editar perfil com sucesso via htmx")
    void deveEditarPerfilHtmx() throws Exception {
        when(passwordValidationService.validar(any(), any(), any(), any())).thenReturn(Optional.empty());

        mockMvc.perform(multipart("/usuario/editar")
                .param("username", "teste2")
                .param("email", "teste@teste.com")
                .param("password", "123")
                .header("HX-Request", "true"))
                .andExpect(status().isOk())
                .andExpect(content().string("Perfil atualizado com sucesso!"));

        verify(usuarioService).atualizarPerfilComImagem(eq(1L), any(UsuarioDTO.class), any());
    }

    @Test
    @DisplayName("Deve editar perfil retornando erro de validacao normal")
    void deveEditarPerfilErroValidacaoNormal() throws Exception {
        when(passwordValidationService.validar(any(), any(), any(), any())).thenReturn(Optional.of("Senha incorreta"));

        mockMvc.perform(multipart("/usuario/editar")
                .param("username", "teste")
                .param("email", "teste@teste.com")
                .param("password", "123"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/Perfil"));
    }
    
    @Test
    @DisplayName("Deve editar perfil retornando erro de validacao via htmx")
    void deveEditarPerfilErroValidacaoHtmx() throws Exception {
        when(passwordValidationService.validar(any(), any(), any(), any())).thenReturn(Optional.of("Senha incorreta"));

        mockMvc.perform(multipart("/usuario/editar")
                .param("username", "teste")
                .param("email", "teste@teste.com")
                .param("password", "123")
                .header("HX-Request", "true"))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Deve dar erro ao atualizar perfil exception normal")
    void deveEditarPerfilExceptionNormal() throws Exception {
        when(passwordValidationService.validar(any(), any(), any(), any())).thenReturn(Optional.empty());
        org.mockito.Mockito.doThrow(new RuntimeException("Erro inesperado")).when(usuarioService).atualizarPerfilComImagem(anyLong(), any(), any());

        mockMvc.perform(multipart("/usuario/editar")
                .param("username", "teste2")
                .param("email", "teste@teste.com")
                .param("password", "123"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/Perfil"));
    }

    @Test
    @DisplayName("Deve dar erro ao atualizar perfil exception htmx")
    void deveEditarPerfilExceptionHtmx() throws Exception {
        when(passwordValidationService.validar(any(), any(), any(), any())).thenReturn(Optional.empty());
        org.mockito.Mockito.doThrow(new RuntimeException("Erro inesperado")).when(usuarioService).atualizarPerfilComImagem(anyLong(), any(), any());

        mockMvc.perform(multipart("/usuario/editar")
                .param("username", "teste2")
                .param("email", "teste@teste.com")
                .param("password", "123")
                .header("HX-Request", "true"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Deve obter imagem do usuario")
    void deveObterImagem() throws Exception {
        ByteArrayResource resource = new ByteArrayResource(new byte[]{1, 2, 3}) {
            @Override
            public String getFilename() {
                return "foto.jpg";
            }
        };
        when(usuarioService.obterImagemPerfil(1L)).thenReturn(resource);

        mockMvc.perform(get("/usuario/1/imagem"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"foto.jpg\""));
    }

    @Test
    @DisplayName("Deve obter imagem do usuario falha")
    void deveObterImagemFalha() throws Exception {
        when(usuarioService.obterImagemPerfil(1L)).thenThrow(new RuntimeException("Erro"));

        mockMvc.perform(get("/usuario/1/imagem"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve verificar status autenticado")
    void deveVerificarStatusAutenticado() throws Exception {
        mockMvc.perform(get("/auth/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.autenticado").value(true))
                .andExpect(jsonPath("$.username").value("Teste"))
                .andExpect(jsonPath("$.cargo").value(Cargo.NUTRICIONISTA.name()));
    }
}
