package com.example.sistemanutricao.aop;

import com.example.sistemanutricao.model.AtividadeLog;
import com.example.sistemanutricao.model.Usuario;
import com.example.sistemanutricao.repository.AtividadeLogRepository;
import com.example.sistemanutricao.security.UsuarioSecurity;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Aspect
@Component
public class AtividadeLogAspect {

    private final AtividadeLogRepository atividadeLogRepository;

    public AtividadeLogAspect(AtividadeLogRepository atividadeLogRepository) {
        this.atividadeLogRepository = atividadeLogRepository;
    }

    @AfterReturning(pointcut = "@annotation(registrarAcao)", returning = "result")
    public void logAfter(JoinPoint joinPoint, RegistrarAcao registrarAcao, Object result) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof UsuarioSecurity usuarioSecurity) {
                Usuario usuario = usuarioSecurity.getUsuario();
                if (usuario != null) {
                    AtividadeLog log = new AtividadeLog(
                            LocalDateTime.now(),
                            usuario,
                            registrarAcao.acao(),
                            registrarAcao.acao().getDescricao()
                    );
                    atividadeLogRepository.save(log);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
