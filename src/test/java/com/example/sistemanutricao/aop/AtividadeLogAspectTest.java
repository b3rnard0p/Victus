package com.example.sistemanutricao.aop;

import com.example.sistemanutricao.model.AtividadeLog;
import com.example.sistemanutricao.model.Usuario;
import com.example.sistemanutricao.model.enuns.TipoAcao;
import com.example.sistemanutricao.repository.AtividadeLogRepository;
import com.example.sistemanutricao.security.UsuarioSecurity;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AtividadeLogAspectTest {

    @Mock
    private AtividadeLogRepository atividadeLogRepository;

    @Mock
    private JoinPoint joinPoint;

    @Mock
    private RegistrarAcao registrarAcao;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private UsuarioSecurity usuarioSecurity;

    @InjectMocks
    private AtividadeLogAspect atividadeLogAspect;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void logAfter_QuandoAutenticado_DeveSalvarLog() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(usuarioSecurity);
        when(usuarioSecurity.getUsuario()).thenReturn(usuario);
        when(registrarAcao.acao()).thenReturn(TipoAcao.CRIOU_FICHA);

        atividadeLogAspect.logAfter(joinPoint, registrarAcao, null);

        verify(atividadeLogRepository, times(1)).save(any(AtividadeLog.class));
    }

    @Test
    void logAfter_QuandoPrincipalDiferente_NaoDeveSalvarLog() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("anonymousUser"); // Not a UsuarioSecurity

        atividadeLogAspect.logAfter(joinPoint, registrarAcao, null);

        verify(atividadeLogRepository, never()).save(any(AtividadeLog.class));
    }

    @Test
    void logAfter_QuandoAutenticacaoNula_NaoDeveSalvarLog() {
        when(securityContext.getAuthentication()).thenReturn(null);

        atividadeLogAspect.logAfter(joinPoint, registrarAcao, null);

        verify(atividadeLogRepository, never()).save(any(AtividadeLog.class));
    }

    @Test
    void logAfter_QuandoLancaExcecao_DeveTratarExceptionSilenciosamente() {
        when(securityContext.getAuthentication()).thenThrow(new RuntimeException("Simulated error"));

        // Metodo não deve jogar a exception para cima, deve printar no console
        atividadeLogAspect.logAfter(joinPoint, registrarAcao, null);

        verify(atividadeLogRepository, never()).save(any(AtividadeLog.class));
    }
}
