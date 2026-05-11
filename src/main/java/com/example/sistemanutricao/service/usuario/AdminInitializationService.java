package com.example.sistemanutricao.service.usuario;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.bootstrap.enabled", havingValue = "true", matchIfMissing = true)
public class AdminInitializationService implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdminInitializationService.class);
    
    private final UsuarioService usuarioService;

    public AdminInitializationService(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("Verificando se existe usuário admin no sistema...");
        usuarioService.inicializarAdminPadrao();
    }
}
