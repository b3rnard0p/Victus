package com.example.sistemanutricao.service.taco;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.example.sistemanutricao.SistemaNutricaoApplication;
import com.example.sistemanutricao.repository.IngredienteRepository;
import com.example.sistemanutricao.repository.UsuarioRepository;

@SpringBootTest(classes = SistemaNutricaoApplication.class)
@ActiveProfiles("test")
@TestPropertySource(properties = "app.bootstrap.enabled=true")
@DisplayName("Bootstrap - Teste de Inicialização TACO")
class TacoBootstrapIntegrationTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private IngredienteRepository ingredienteRepository;

    @Test
    @DisplayName("Deve criar usuário TACO e importar ingredientes no startup")
    void deveImportarTacoNoStartup() {
        // O CommandLineRunner (TacoInitializationService) roda automaticamente
        // durante a inicialização do contexto do Spring devido ao @SpringBootTest
        
        boolean usuarioExiste = usuarioRepository.findByUsernameIgnoreCase("TACO").isPresent();
        long totalIngredientes = ingredienteRepository.count();

        assertTrue(usuarioExiste, "O usuário TACO deveria ter sido criado automaticamente.");
        assertTrue(totalIngredientes > 0, "Deveriam ter sido importados ingredientes da tabela TACO para o banco de dados.");
    }
}
