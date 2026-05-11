package com.example.sistemanutricao.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.sistemanutricao.BaseIntegrationTest;
import com.example.sistemanutricao.model.Ingrediente;
import com.example.sistemanutricao.model.enuns.Cargo;
import com.example.sistemanutricao.model.enuns.Status;
import com.example.sistemanutricao.model.Usuario;
import com.example.sistemanutricao.repository.IngredienteRepository;
import com.example.sistemanutricao.repository.UsuarioRepository;
import com.example.sistemanutricao.security.UsuarioSecurity;

@DisplayName("Ingrediente - Teste de Integração")
class IngredienteIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private IngredienteRepository ingredienteRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Usuario nutricaoUser;
    private UsuarioSecurity nutricaoSecurity;

    @BeforeEach
    void setUp() {
        ingredienteRepository.deleteAll();
        usuarioRepository.deleteAll();

        nutricaoUser = new Usuario();
        nutricaoUser.setUsername("nutri_test");
        nutricaoUser.setEmail("nutri@test.com");
        nutricaoUser.setSenha(passwordEncoder.encode("password"));
        nutricaoUser.setCargo(Cargo.NUTRICIONISTA);
        nutricaoUser.setAtivo(true);
        nutricaoUser = usuarioRepository.save(nutricaoUser);

        nutricaoSecurity = new UsuarioSecurity(nutricaoUser);
    }

    @Test
    @DisplayName("Deve listar ingredientes do banco de dados")
    void deveListarIngredientes() throws Exception {
        // GIVEN
        Ingrediente ingrediente = new Ingrediente();
        ingrediente.setNome("Arroz Integral");
        ingrediente.setPtn(new BigDecimal("2.6"));
        ingrediente.setCho(new BigDecimal("25.8"));
        ingrediente.setLip(new BigDecimal("1.0"));
        ingrediente.setSodio(new BigDecimal("1.0"));
        ingrediente.setGorduraSaturada(new BigDecimal("0.2"));
        ingrediente.setStatus(Status.ATIVA);
        ingrediente.setUsuario(nutricaoUser);
        ingredienteRepository.save(ingrediente);


        // WHEN & THEN
        mockMvc.perform(get("/ingrediente").with(user(nutricaoSecurity)))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("ingredientes"))
                .andExpect(view().name("Layout")); // PaginacaoViewSupport returns Layout when not HTMX
    }
}
