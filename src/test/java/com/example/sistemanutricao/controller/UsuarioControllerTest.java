package com.example.sistemanutricao.controller;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

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

import com.example.sistemanutricao.model.enuns.Cargo;
import com.example.sistemanutricao.model.Usuario;
import com.example.sistemanutricao.record.UsuarioDTO.GetUsuarioDTO;
import com.example.sistemanutricao.security.AuthSessionService;
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
    @DisplayName("Deve exibir modal de perfil")
    void deveExibirPerfilModal() throws Exception {
        GetUsuarioDTO dto = new GetUsuarioDTO(1L, "nutri", "nutri@example.com", Cargo.NUTRICIONISTA, null, "", true, "");
        when(usuarioService.findById(anyLong())).thenReturn(dto);

        mockMvc.perform(get("/usuario/perfil/modal"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/Perfil"));

        verify(usuarioService).findById(eq(1L));
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
